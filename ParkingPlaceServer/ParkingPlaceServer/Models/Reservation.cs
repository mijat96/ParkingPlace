using System;
using System.Collections.Generic;

namespace ParkingPlaceServer.Models
{
	public class Reservation
	{
        public long Id { get; set; }
        public DateTime StartDateTime { get; set; }
        public DateTime EndDateTime { get; set; }
        public ParkingPlace ParkingPlace { get; set; }
        public User User { get; set; }

        private static readonly double DURATION_OF_RESERVATION = 1.0; // 10.0; // min
        private static int idCounter = 0;

        public Reservation()
        {

        }

        public Reservation(ParkingPlace parkingPlace, User user)
        {
            Id = idCounter++;
            StartDateTime = DateTime.Now;
            EndDateTime = StartDateTime.AddMinutes(DURATION_OF_RESERVATION);
            ParkingPlace = new ParkingPlace(parkingPlace);
            User = user;
            User.Reservation = this;
        }

        public override int GetHashCode()
        {
            int hashCode = 79308497;
            hashCode = hashCode * -1521134295 + Id.GetHashCode();
            hashCode = hashCode * -1521134295 + StartDateTime.GetHashCode();
            hashCode = hashCode * -1521134295 + EndDateTime.GetHashCode();
            hashCode = hashCode * -1521134295 + EqualityComparer<ParkingPlace>.Default.GetHashCode(ParkingPlace);
            hashCode = hashCode * -1521134295 + EqualityComparer<User>.Default.GetHashCode(User);
            return hashCode;
        }

        public override bool Equals(object obj)
        {
            return obj is Reservation reservation &&
                   Id == reservation.Id &&
                   StartDateTime == reservation.StartDateTime &&
                   EndDateTime == reservation.EndDateTime &&
                   EqualityComparer<ParkingPlace>.Default.Equals(ParkingPlace, reservation.ParkingPlace) &&
                   EqualityComparer<User>.Default.Equals(User, reservation.User);
        }
    }
}