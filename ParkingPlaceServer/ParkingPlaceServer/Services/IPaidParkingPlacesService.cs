using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public interface IPaidParkingPlacesService
	{
		List<PaidParkingPlace> getPaidParkingPlaces();
		void AddPaidParkingPlace(PaidParkingPlace paidParkingPlace);
	}
}