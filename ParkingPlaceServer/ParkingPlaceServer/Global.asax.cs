
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
			List<Reservation> reservations = reservationsService.getReservations();
			if (reservations.Count == 0)
			{
				return;
			}

			List<Reservation> reservationsForRemoving = new List<Reservation>();

			Zone zone;
			ParkingPlace parkingPlace;

			foreach (Reservation reservation in reservations)
			{
				if (reservation.EndDateTime < DateTime.Now)
				{
					zone = zonesService.getZone(reservation.ParkingPlace.Zone.Id);
					parkingPlace = zone.getParkingPlace(reservation.ParkingPlace.Id);
					parkingPlace.Status = ParkingPlaceStatus.EMPTY;
					reservation.User.AddViolation(true);
				}
			}

			if (reservationsForRemoving.Count > 0)
			{
				reservations.RemoveAll(item => reservationsForRemoving.Contains(item));
			}
		
		}

		private static void CheckAndHandlePaidParkingPlaces()
		{
			List<PaidParkingPlace> paidParkingPlaces = paidParkingPlacesService.getPaidParkingPlaces();
			if (paidParkingPlaces.Count == 0)
			{
				return;
			}

			List<PaidParkingPlace> paidParkingPlacesForRemoving = new List<PaidParkingPlace>();

			Zone zone;
			ParkingPlace parkingPlace;

			foreach (PaidParkingPlace paidParkingPlace in paidParkingPlaces)
			{
				if (paidParkingPlace.GetEndDateTime() < DateTime.Now)
				{
					zone = zonesService.getZone(paidParkingPlace.ParkingPlace.Zone.Id);
					parkingPlace = zone.getParkingPlace(paidParkingPlace.ParkingPlace.Id);
					parkingPlace.Status = ParkingPlaceStatus.EMPTY;
					paidParkingPlace.User.AddViolation(false);
				}
			}

			if (paidParkingPlacesForRemoving.Count > 0)
			{
				paidParkingPlaces.RemoveAll(item => paidParkingPlacesForRemoving.Contains(item));
			}
			
		}

	}

}
