using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public interface IUsersService
	{
		List<User> getUsers();
		
		User getUser(string username);

		User getLoggedUser(string token);
		string login(string username, string password);
	}
}