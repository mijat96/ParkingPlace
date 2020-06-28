using Microsoft.Ajax.Utilities;
using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Models;
using ParkingPlaceServer.Models.Security;
using ParkingPlaceServer.Services;
using ParkingPlaceServer.Utils;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Http;

namespace ParkingPlaceServer.Controllers
{
	public class ParkingPlacesController : ApiController
	{
		private IZonesService zonesService = ZonesService.Instance;
		private IReservationsService reservationsService = ReservationsService.Instance;
		private IPaidParkingPlacesService paidParkingPlacesService = PaidParkingPlacesService.Instance;
		private IUsersService usersService = UsersService.Instance;
		private static readonly double MAX_DISTANCE_FOR_RESERVATION = 5000.0; // meteres
		private static readonly double MAX_DISTANCE_FOR_TAKING = 1.0; // meters

		[Route("api/parkingplaces/reservation")]
		public async Task<HttpResponseMessage> PostReservation([FromBody] ReservingDto value)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			Zone zone = null;
			try
			{
				zone = zonesService.GetZone(value.ZoneId);
			}
			catch (Exception e)
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
			}

			ParkingPlace parkingPlace = null;
			try
			{
				parkingPlace = zone.ParkingPlaces
					.Where(pp => pp.Id == value.ParkingPlaceId)
					.Single();
			}
			catch (Exception e)
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
			}


			Reservation reservation;
			lock (parkingPlace)
			{
				if (parkingPlace.Status != ParkingPlaceStatus.EMPTY)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest, "parkingPlace.Status != ParkingPlaceStatus.EMPTY");
				}

				double distance = Distance.computeDistance(value.CurrentLocationLatitude, 
															value.CurrentLocationLongitude,
															parkingPlace.Location.Latitude,
															parkingPlace.Location.Longitude);
				if (distance > MAX_DISTANCE_FOR_RESERVATION)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest);
				}

				parkingPlace.Status = ParkingPlaceStatus.RESERVED;
				reservation = new Reservation(parkingPlace, usersService.GetLoggedUser(token), value.DateTimeAndroid);
				reservationsService.AddReservation(reservation);

				lock (parkingPlace.Zone)
				{
					parkingPlace.Zone.Version++;
					parkingPlace.Zone.AddParkingPlaceChange(parkingPlace.Id, parkingPlace.Status);
				}
			}

			return Request.CreateResponse(HttpStatusCode.OK, new ReservationDTO(reservation));
		}

		[Route("api/parkingplaces/taking")]
		public async Task<HttpResponseMessage> PostTaking([FromBody] TakingDTO value)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			User loggedUser = usersService.GetLoggedUser(token);

			if (value.TicketType != TicketType.REGULAR)
			{
				bool nearByFavoritePlace = paidParkingPlacesService.CheckWheterIsParkingPlaceNearByFavoritePlace(
																					loggedUser.FavoritePlaces,
																					value.CurrentLocationLatitude,
																					value.CurrentLocationLongitude);
				if (!nearByFavoritePlace)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest);
				}
			}

			bool reservationFoundedAndRemoved = reservationsService.RemoveReservation(loggedUser);

			Zone zone = null;
			try
			{
				zone = zonesService.GetZone(value.ZoneId);
			}
			catch (Exception e)
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
			}

			ParkingPlace parkingPlace = null;
			try
			{
				parkingPlace = zone.ParkingPlaces
					.Where(pp => pp.Id == value.ParkingPlaceId)
					.Single();
			}
			catch (Exception e)
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
			}

			PaidParkingPlace paidParkingPlace;
			lock (parkingPlace)
			{
				if (parkingPlace.Status == ParkingPlaceStatus.TAKEN)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest, "parkingPlace.Status == ParkingPlaceStatus.TAKEN");
				}
				else if (parkingPlace.Status == ParkingPlaceStatus.RESERVED && !reservationFoundedAndRemoved)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest, "parkingPlace.Status == ParkingPlaceStatus.RESERVED && !reservationRemoved");
				}

				double distance = Distance.computeDistance(value.CurrentLocationLatitude,
															value.CurrentLocationLongitude,
															parkingPlace.Location.Latitude,
															parkingPlace.Location.Longitude);
				if (distance > MAX_DISTANCE_FOR_TAKING)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest);
				}


				parkingPlace.Status = ParkingPlaceStatus.TAKEN;
				paidParkingPlace = new PaidParkingPlace(parkingPlace, loggedUser, value.DateTimeAndroid, value.TicketType);
				paidParkingPlacesService.AddPaidParkingPlace(paidParkingPlace);

				lock (parkingPlace.Zone)
				{
					parkingPlace.Zone.Version++;
					parkingPlace.Zone.AddParkingPlaceChange(parkingPlace.Id, parkingPlace.Status);
				}
			}

			return Request.CreateResponse(HttpStatusCode.OK, new PaidParkingPlaceDTO(paidParkingPlace));
		}

		[Route("api/parkingplaces/leave")]
		[HttpPut]
		public async Task<HttpResponseMessage> LeaveParkingPlace(Dto value)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			User loggedUser = usersService.GetLoggedUser(token);

			bool paidParkingPlaceFoundedAndRemoved = paidParkingPlacesService.RemovePaidParkingPlace(loggedUser, value.ParkingPlaceId);

			if (paidParkingPlaceFoundedAndRemoved)
			{
				Zone zone = null;
				try
				{
					zone = zonesService.GetZone(value.ZoneId);
				}
				catch (Exception e)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
				}

				ParkingPlace parkingPlace = null;
				try
				{
					parkingPlace = zone.ParkingPlaces
						.Where(pp => pp.Id == value.ParkingPlaceId)
						.Single();
				}
				catch (Exception e)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
				}

				lock (parkingPlace)
				{
					if (parkingPlace.Status != ParkingPlaceStatus.TAKEN)
					{
						return Request.CreateResponse(HttpStatusCode.BadRequest, "parkingPlace.Status == ParkingPlaceStatus.TAKEN");
					}

					parkingPlace.Status = ParkingPlaceStatus.EMPTY;

					lock (parkingPlace.Zone)
					{
						parkingPlace.Zone.Version++;
						parkingPlace.Zone.AddParkingPlaceChange(parkingPlace.Id, parkingPlace.Status);
					}
				}

				return Request.CreateResponse(HttpStatusCode.OK);
			}

			return Request.CreateResponse(HttpStatusCode.BadRequest);
		}

		private string GetHeader(string key)
		{
			IEnumerable<string> keys = null;
			if (!Request.Headers.TryGetValues(key, out keys))
				return null;

			return keys.First();
		}

		[Route("api/parkingplaces/changes")]
		public async Task<HttpResponseMessage> GetParkingPlaceChanges([FromUri] string zoneids, [FromUri] string versions)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			if (string.IsNullOrEmpty(zoneids) || string.IsNullOrEmpty(versions))
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest);
			}

			long[] zoneIdsArray, versionsArray;

			convertStringToLongArray(zoneids, out zoneIdsArray);
			convertStringToLongArray(versions, out versionsArray);

			List<Zone> zones = zonesService.GetZones(zoneIdsArray);

			List<ParkingPlaceChangesDTO> changes = new List<ParkingPlaceChangesDTO>();
			List<ParkingPlacesInitialDTO> initials = new List<ParkingPlacesInitialDTO>();


			int parkingPlaceChangesCount;
			int versionDiff;
			List<ParkingPlaceDTO> myParkingPlaceChanges;
			Zone zone;
			long version;

			for (int i = 0; i < zones.Count; i++)
			{
				version = versionsArray[i];
				zone = zones[i];
				lock(zone)
				{
					if (version == -1)
					{
						initials.Add(new ParkingPlacesInitialDTO(zone.Id, zone.Version, zone.ParkingPlaces));
					}
					else if (zone.Version > version)
					{
						versionDiff = (int)(zone.Version - version);

						if (versionDiff > Zone.PARKING_PLACE_CHANGES_MAX_SIZE)
						{
							initials.Add(new ParkingPlacesInitialDTO(zone.Id, zone.Version, zone.ParkingPlaces));
						}
						else
						{
							parkingPlaceChangesCount = zone.ParkingPlaceChanges.Count;
							myParkingPlaceChanges = zone.ParkingPlaceChanges
														.Where(ppc => ppc.Value.Version > version)
														.Select(ppc => new ParkingPlaceDTO(ppc.Value.Id, ppc.Value.Status))
														.ToList();
							changes.Add(new ParkingPlaceChangesDTO(zone.Id, zone.Version, myParkingPlaceChanges));
						}

					}
				}
				
			}

			return Request.CreateResponse(HttpStatusCode.OK, new ParkingPlacesUpdatingDTO(changes, initials));
		}

		[Route("api/parking/place/getParking/{id}")]
		public async Task<HttpResponseMessage> GetParkingPlace(long id)
        {
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			User loggedUser = usersService.GetLoggedUser(token);

			bool againTaked = paidParkingPlacesService.AgainTakeParkingPlace(loggedUser, id);

			if (!againTaked)
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest);
			}

			ParkingPlaceForReservationInNotificationDTO parking =
				new ParkingPlaceForReservationInNotificationDTO();
			return Request.CreateResponse(HttpStatusCode.OK, parking);

			//string token = GetHeader("token");
			//if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			//{
			//	return Request.CreateResponse(HttpStatusCode.Unauthorized);
			//}

			//List<Zone> zones = null;
			//try
			//{
			//	zones = zonesService.GetZones();
			//}
			//catch (Exception e)
			//{
			//	return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
			//}

			//ParkingPlace parkingPlace = null;
			//try
			//{
			//	foreach(Zone z in zones)
			//             {
			//                 foreach(ParkingPlace p in z.ParkingPlaces)
			//                 {
			//                     if (p.Id.Equals(id))
			//                     {
			//				parkingPlace = p;
			//				parkingPlace.Zone = z;
			//				break;

			//			}
			//                 }
			//             }
			//}
			//catch (Exception e)
			//{
			//	return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
			//}

			//ParkingPlaceForReservationInNotificationDTO parking =
			//	new ParkingPlaceForReservationInNotificationDTO(parkingPlace);

			//return Request.CreateResponse(HttpStatusCode.OK, parking);
		}

		[Route("api/parking/place/againTake/{id}")]
		public async Task<HttpResponseMessage> AgainTake(long id)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			User loggedUser = usersService.GetLoggedUser(token);

			bool againTaked = paidParkingPlacesService.AgainTakeParkingPlace(loggedUser, id);

            if (!againTaked)
            {
				return Request.CreateResponse(HttpStatusCode.BadRequest);
            }

			ParkingPlaceForReservationInNotificationDTO parking =
				new ParkingPlaceForReservationInNotificationDTO();
			return Request.CreateResponse(HttpStatusCode.OK, parking);
		}

		private bool convertStringToLongArray(string str, out long[] array)
		{
			
			string[] strTokens = str.Split(',');
			array = new long[strTokens.Length];


			for (int i = 0; i < strTokens.Length; i++)
			{
				if (!long.TryParse(strTokens[i], out array[i]))
				{
					return false;
				}
			}

			return true;
		}

	}
}
