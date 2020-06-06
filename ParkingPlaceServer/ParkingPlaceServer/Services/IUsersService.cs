using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public interface IUsersService
	{
		List<User> GetUsers();
		
		User GetUser(string username);

		User GetLoggedUser(string token);

		string Login(string username, string password);
		
		void Register(string username, string password, string repeatPassword, string carRegistrationNumber);

		long AddOrUpdateFavoritePlace(User loggedUser, FavoritePlace favoritePlace);
		void RemoveFavoritePlace(User loggedUser, long favoritePlaceId);
		ReservationAndPaidParkingPlacesDTO GetReservationAndPaidParkingPlaces(User loggedUser);
	}
}