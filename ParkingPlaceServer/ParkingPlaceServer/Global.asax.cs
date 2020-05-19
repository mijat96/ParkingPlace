
using ParkingPlaceServer.Models;
using ParkingPlaceServer.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Timers;
using System.Web;
using System.Web.Http;
using System.Web.Mvc;
using System.Web.Optimization;
using System.Web.Routing;

namespace ParkingPlaceServer
{
	public class WebApiApplication : System.Web.HttpApplication
	{
		private static IReservationsService reservationsService = ReservationsService.Instance;
		private static IPaidParkingPlacesService paidParkingPlacesService = PaidParkingPlacesService.Instance;

		private static IZonesService zonesService = ZonesService.Instance;

		protected void Application_Start()
		{
			AreaRegistration.RegisterAllAreas();
			System.Web.Http.GlobalConfiguration.Configure(WebApiConfig.Register);
			FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
			RouteConfig.RegisterRoutes(RouteTable.Routes);
			BundleConfig.RegisterBundles(BundleTable.Bundles);

			System.Timers.Timer aTimer = new System.Timers.Timer();
			aTimer.Elapsed += new ElapsedEventHandler(OnTimedEvent);
			aTimer.Interval = 1000; // 1 sec
			aTimer.Enabled = true;

		}

		private static void OnTimedEvent(object source, ElapsedEventArgs e)
		{
			CheckAndHandleReservations();
			CheckAndHandlePaidParkingPlaces();
		}

		private static void CheckAndHandleReservations()
		{
			List<Reservation> reservations = reservationsService.GetReservations();
			if (reservations.Count == 0)
			{
				return;
			}

			lock (reservations)
			{
				List<Reservation> reservationsForRemoving = new List<Reservation>();

				Zone zone;
				ParkingPlace parkingPlace;

				foreach (Reservation reservation in reservations)
				{
					if (reservation.EndDateTime < DateTime.Now)
					{
						zone = zonesService.GetZone(reservation.ParkingPlace.Zone.Id);
						lock (zone)
						{
							parkingPlace = zone.GetParkingPlace(reservation.ParkingPlace.Id);
							lock (parkingPlace)
							{
								parkingPlace.Status = ParkingPlaceStatus.EMPTY;
							}

							zone.Version++;
							zone.AddParkingPlaceChange(parkingPlace.Id, parkingPlace.Status);
						}
						reservation.User.AddViolation(true);

						reservationsForRemoving.Add(reservation);
					}
				}

				if (reservationsForRemoving.Count > 0)
				{
					reservations.RemoveAll(item => reservationsForRemoving.Contains(item));
				}
			}
		
		}

		private static void CheckAndHandlePaidParkingPlaces()
		{
			List<PaidParkingPlace> paidParkingPlaces = paidParkingPlacesService.GetPaidParkingPlaces();
			if (paidParkingPlaces.Count == 0)
			{
				return;
			}

			lock (paidParkingPlaces)
			{
				List<PaidParkingPlace> paidParkingPlacesForRemoving = new List<PaidParkingPlace>();

				Zone zone;
				ParkingPlace parkingPlace;

				foreach (PaidParkingPlace paidParkingPlace in paidParkingPlaces)
				{
					if (paidParkingPlace.GetEndDateTime() < DateTime.Now)
					{
						zone = zonesService.GetZone(paidParkingPlace.ParkingPlace.Zone.Id);
						lock (zone)
						{
							parkingPlace = zone.GetParkingPlace(paidParkingPlace.ParkingPlace.Id);
							lock (parkingPlace)
							{
								parkingPlace.Status = ParkingPlaceStatus.EMPTY;
							}

							zone.Version++;
							zone.AddParkingPlaceChange(parkingPlace.Id, parkingPlace.Status);
						}
						paidParkingPlace.User.AddViolation(false);

						paidParkingPlacesForRemoving.Add(paidParkingPlace);
					}
				}

				if (paidParkingPlacesForRemoving.Count > 0)
				{
					paidParkingPlaces.RemoveAll(item => paidParkingPlacesForRemoving.Contains(item));
				}
			}
			
		}

	}

}
