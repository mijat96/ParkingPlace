using ParkingPlaceServer.DAO;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public class ReservationsService : IReservationsService
	{
		private static object lockObject = new object();

		private static ReservationsService instance = null;

		public static ReservationsService Instance
		{
			get
			{
				lock (lockObject)
				{
					if (instance == null)
					{
						instance = new ReservationsService();
					}
				}


				return instance;
			}
		}

		private ReservationDAO reservationDAO;

		private ReservationsService()
		{
			reservationDAO = new ReservationDAO();
		}

		public List<Reservation> getReservations()
		{
			return reservationDAO.getReservations();
		}

		public void AddReservation(Reservation reservation)
		{
			reservationDAO.AddReservation(reservation);
		}
	}
}