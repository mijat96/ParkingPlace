using ParkingPlaceServer.DAO;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public class UsersService : IUsersService
	{
		private static object lockObject = new object();

		private static UsersService instance = null;

		public static UsersService Instance
		{
			get
			{
				lock (lockObject)
				{
					if (instance == null)
					{
						instance = new UsersService();
					}
				}


				return instance;
			}
		}

		private UserDAO userDAO;
		private User loggedUser;

		private UsersService()
		{
			userDAO = new UserDAO();
		}

		public List<User> getUsers()
		{
			return userDAO.getUsers();
		}

		public User getUser(string username)
		{
			return userDAO.getUsers()
					.Where(u => u.Username.Equals(username))
					.Single();
		}

		public User getLoggedUser(string token)
		{
			string username = Models.Security.TokenManager.GetUsername(token);
			return userDAO.getUsers()
				.Where(u => u.Username.Equals(username))
				.Single();
		}

		public string login(string username, string password)
		{
			User user = null;
			try
			{
				user = userDAO.getUsers()
								.Where(u => u.Username.Equals(username) && u.Password.Equals(password))
								.Single();
			}
			catch (Exception e)
			{
				throw new InvalidUsernameOrPasswordException("Invalid username or password!");
			}

			string token = Models.Security.TokenManager.GenerateToken(username);
			return token;
		}
	}
}