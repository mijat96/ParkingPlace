using System;
using System.Collections.Generic;

namespace ParkingPlaceServer.Models
{
	public class PaidParkingPlace
	{
		public ParkingPlace ParkingPlace { get; set; }
		public DateTime StartDateTime { get; set; }
		public TicketType TicketType { get; set; }
		public bool ArrogantUser { get; set; }
		public User User { get; set; }

		public PaidParkingPlace()
		{

		}

		public PaidParkingPlace(ParkingPlace parkingPlace, User user, TicketType ticketType)
		{
			ParkingPlace = new ParkingPlace(parkingPlace);
			StartDateTime = DateTime.Now;
			TicketType = ticketType;
			ArrogantUser = false;
			User = user;
			User.RegularPaidParkingPlace = this;
		}

		public DateTime GetEndDateTime()
		{
			Zone zone = ParkingPlace.Zone;
			TicketPrice ticketPrice = zone.GetTicketPrice(TicketType);
			return StartDateTime.AddHours(ticketPrice.Duration);
		}

		public override bool Equals(object obj)
		{
			return obj is PaidParkingPlace place &&
				   EqualityComparer<ParkingPlace>.Default.Equals(ParkingPlace, place.ParkingPlace) &&
				   StartDateTime == place.StartDateTime &&
				   TicketType == place.TicketType &&
				   ArrogantUser == place.ArrogantUser &&
				   EqualityComparer<User>.Default.Equals(User, place.User);
		}

		public override int GetHashCode()
		{
			int hashCode = -1744307677;
			hashCode = hashCode * -1521134295 + EqualityComparer<ParkingPlace>.Default.GetHashCode(ParkingPlace);
			hashCode = hashCode * -1521134295 + StartDateTime.GetHashCode();
			hashCode = hashCode * -1521134295 + TicketType.GetHashCode();
			hashCode = hashCode * -1521134295 + ArrogantUser.GetHashCode();
			hashCode = hashCode * -1521134295 + EqualityComparer<User>.Default.GetHashCode(User);
			return hashCode;
		}
	}
}