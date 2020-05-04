using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Models;
using ParkingPlaceServer.Models.Security;
using ParkingPlaceServer.Services;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace ParkingPlaceServer.Controllers
{
	public class ParkingPlacesController : ApiController
	{
		private IZonesService zonesService = ZonesService.Instance;
		private IReservationsService reservationsService = ReservationsService.Instance;
		private IPaidParkingPlacesService paidParkingPlacesService = PaidParkingPlacesService.Instance;
		private IUsersService usersService = UsersService.Instance;


		[Route("api/parkingplaces/reservation")]
		public HttpResponseMessage PostReservation([FromBody] ReservationDTO value)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			Zone zone = null;
			try
			{
				zone = zonesService.getZone(value.ZoneId);
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
			
			lock(parkingPlace)
			{
				if (parkingPlace.Status != ParkingPlaceStatus.EMPTY)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest, "parkingPlace.Status != ParkingPlaceStatus.EMPTY");
				}

				parkingPlace.Status = ParkingPlaceStatus.RESERVED;
				reservationsService.AddReservation(new Reservation(parkingPlace, usersService.getLoggedUser(token)));

				lock (parkingPlace.Zone)
				{
					parkingPlace.Zone.ParkingPlaceChanges.Add(new ParkingPlaceDTO(parkingPlace.Id, parkingPlace.Status));
					parkingPlace.Zone.Version++;
				}
			}

			return Request.CreateResponse(HttpStatusCode.OK);
		}

		[Route("api/parkingplaces/taking")]
		public HttpResponseMessage PostTaking([FromBody] TakingDTO value)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			Zone zone = null;
			try
			{
				zone = zonesService.getZone(value.ZoneId);
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
				if (parkingPlace.Status != ParkingPlaceStatus.EMPTY)
				{
					return Request.CreateResponse(HttpStatusCode.BadRequest, "parkingPlace.Status != ParkingPlaceStatus.EMPTY");
				}
				
				parkingPlace.Status = ParkingPlaceStatus.TAKEN;
				paidParkingPlacesService.AddPaidParkingPlace(new PaidParkingPlace(parkingPlace, 
																				usersService.getLoggedUser(token)));

				lock (parkingPlace.Zone)
				{
					parkingPlace.Zone.ParkingPlaceChanges.Add(new ParkingPlaceDTO(parkingPlace.Id, parkingPlace.Status));
					parkingPlace.Zone.Version++;
				}
			}

			return Request.CreateResponse(HttpStatusCode.OK);
		}

		private string GetHeader(string key)
		{
			IEnumerable<string> keys = null;
			if (!Request.Headers.TryGetValues(key, out keys))
				return null;

			return keys.First();
		}

		[Route("api/parkingplaces/changes")]
		public HttpResponseMessage GetParkingPlaceChanges([FromUri] long[] zoneIds, [FromUri] long[] versions)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			List<Zone> zones = zonesService.getZones(zoneIds);

			List<ParkingPlaceChangesDTO> parkingPlaceChangesDTOs = new List<ParkingPlaceChangesDTO>();

			ParkingPlaceChangesDTO parkingPlaceChangesDTO;
			int parkingPlaceChangesCount;
			int length;
			List<ParkingPlaceDTO> myParkingPlaceChanges;

			for (int i = 0; i < zones.Count; i++)
			{	
				if (zones[i].Version > versions[i])
				{
					length = (int) (zones[i].Version - versions[i]);
					parkingPlaceChangesCount = zones[i].ParkingPlaceChanges.Count;
					myParkingPlaceChanges = zones[i].ParkingPlaceChanges
													.GetRange(parkingPlaceChangesCount - length, length);
				}
				else
				{
					myParkingPlaceChanges = new List<ParkingPlaceDTO>();
				}

				parkingPlaceChangesDTO = new ParkingPlaceChangesDTO(zones[i].Id, zones[i].Version, myParkingPlaceChanges);
				parkingPlaceChangesDTOs.Add(parkingPlaceChangesDTO);
			}

			return Request.CreateResponse(HttpStatusCode.OK, parkingPlaceChangesDTOs);
		}


	}
}
