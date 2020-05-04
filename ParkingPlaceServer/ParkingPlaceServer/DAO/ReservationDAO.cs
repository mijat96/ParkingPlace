using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.IO;

namespace ParkingPlaceServer.Services
{
	public class ReservationDAO
	{


        private List<Reservation> reservations = null;

        public List<Reservation> getReservations()
        {
            if (reservations == null)
            {
                reservations = new List<Reservation>();
            }

            return reservations;
        }

		public void AddReservation(Reservation reservation)
		{
            reservations.Add(reservation);
		}
	}
}