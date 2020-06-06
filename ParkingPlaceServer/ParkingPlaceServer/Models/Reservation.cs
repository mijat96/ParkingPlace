using System;
using System.Collections.Generic;
using System.Globalization;

namespace ParkingPlaceServer.Models
{
	public class Reservation
	{
        public long Id { get; set; }
        public DateTime StartDateTimeAndroid { get; set; }
        public DateTime StartDateTimeServer { get; set; }
        public ParkingPlace ParkingPlace { get; set; }
        public User User { get; set; }

        private static readonly double DURATION_OF_RESERVATION = 1.0; // 10.0; // min
        private static int idCounter = 0;
        private static readonly string formatSpecifier = "G";
        private static readonly CultureInfo culture = CultureInfo.CreateSpecificCulture("de-DE");

        public Reservation()
        {

        }

        public Reservation(ParkingPlace parkingPlace, User user, string startDateTimeAndroid)
        {
            Id = idCounter++;
            StartDateTimeAndroid = DateTime.ParseExact(startDateTimeAndroid, formatSpecifier, culture);
            StartDateTimeServer = DateTime.Now;
            ParkingPlace = new ParkingPlace(parkingPlace);
            User = user;
            User.Reservation = this;
        }


        public string GetStartDateTimeServerString()
        {
            return StartDateTimeServer.ToString(formatSpecifier, culture);
        }

        public string GetStartDateTimeAndroidString()
        {
            return StartDateTimeAndroid.ToString(formatSpecifier, culture);
        }

        public DateTime GetEndDateTimeAndroid()
        {
            return StartDateTimeAndroid.AddMinutes(DURATION_OF_RESERVATION);
        }

        public DateTime GetEndDateTimeServer()
        {
            return StartDateTimeServer.AddMinutes(DURATION_OF_RESERVATION);
        }

        public override int GetHashCode()
        {
            int hashCode = 79308497;
            hashCode = hashCode * -1521134295 + Id.GetHashCode();
            hashCode = hashCode * -1521134295 + StartDateTimeAndroid.GetHashCode();
            hashCode = hashCode * -1521134295 + StartDateTimeServer.GetHashCode();
            hashCode = hashCode * -1521134295 + EqualityComparer<ParkingPlace>.Default.GetHashCode(ParkingPlace);
            hashCode = hashCode * -1521134295 + EqualityComparer<User>.Default.GetHashCode(User);
            return hashCode;
        }

        public override bool Equals(object obj)
        {
            return obj is Reservation reservation &&
                   Id == reservation.Id &&
                   StartDateTimeAndroid == reservation.StartDateTimeAndroid &&
                   StartDateTimeServer == reservation.StartDateTimeServer &&
                   EqualityComparer<ParkingPlace>.Default.Equals(ParkingPlace, reservation.ParkingPlace) &&
                   EqualityComparer<User>.Default.Equals(User, reservation.User);
        }
    }
}