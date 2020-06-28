using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public interface IPaidParkingPlacesService
	{
		List<PaidParkingPlace> GetPaidParkingPlaces();

		void AddPaidParkingPlace(PaidParkingPlace paidParkingPlace);
		
		bool RemovePaidParkingPlace(User loggedUser, long parkingPlaceId);
		bool CheckWheterIsParkingPlaceNearByFavoritePlace(List<FavoritePlace> favoritePlaces, 
								double currentLocationLatitude, double currentLocationLongitude);

		bool AgainTakeParkingPlace(User loggedUser, long id);
	}
}