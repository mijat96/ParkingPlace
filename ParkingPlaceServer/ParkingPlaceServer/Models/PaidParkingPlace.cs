using System;
using System.Collections.Generic;
using System.Globalization;

namespace ParkingPlaceServer.Models
{
	public class PaidParkingPlace
	{
		public long Id { get; set; }
		public ParkingPlace ParkingPlace { get; set; }
		public DateTime StartDateTimeAndroid { get; set; }

		public DateTime StartDateTimeServer { get; set; }
		public TicketType TicketType { get; set; }
		public bool ArrogantUser { get; set; }
		public User User { get; set; }

        public bool AgainTake { get; set; }

        private static long idGenerator = 0;
		private static readonly string formatSpecifier = "G";
		private static readonly CultureInfo culture = CultureInfo.CreateSpecificCulture("de-DE");

		public PaidParkingPlace()
		{

		}

		public PaidParkingPlace(ParkingPlace parkingPlace, User user, string startDateTimeAndroid, TicketType ticketType)
		{
			Id = idGenerator++;
			ParkingPlace = new ParkingPlace(parkingPlace);
			StartDateTimeAndroid = DateTime.ParseExact(startDateTimeAndroid, formatSpecifier, culture);
			StartDateTimeServer = DateTime.Now;
			TicketType = ticketType;
			ArrogantUser = false;
			User = user;
			User.RegularPaidParkingPlace = this;
			AgainTake = false;
		}

		public DateTime GetEndDateTimeAndroid()
		{
			Zone zone = ParkingPlace.Zone;
			TicketPrice ticketPrice = zone.GetTicketPrice(TicketType);
			return StartDateTimeAndroid.AddHours(ticketPrice.Duration);
		}

		public DateTime GetEndDateTimeServer()
		{
			Zone zone = ParkingPlace.Zone;
			TicketPrice ticketPrice = zone.GetTicketPrice(TicketType);
			return StartDateTimeServer.AddHours(ticketPrice.Duration);
		}

		public string GetStartDateTimeServerString()
		{
			return StartDateTimeServer.ToString(formatSpecifier, culture);
		}


		public string GetStartDateTimeAndroidString()
		{
			return StartDateTimeAndroid.ToString(formatSpecifier, culture);
		}

		public void LeavePaidParkingPlaceInUser()
		{
			User.LeavePaidParkingPlace(this);
		}

		public override bool Equals(object obj)
		{
			return obj is PaidParkingPlace place &&
				   Id == place.Id;
		}

		public override int GetHashCode()
		{
			return 2108858624 + Id.GetHashCode();
		}

	}
}