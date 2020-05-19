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

		public List<User> GetUsers()
		{
			return userDAO.Users;
		}

		public User GetUser(string username)
		{
			return userDAO.Users
					.Where(u => u.Username.Equals(username))
					.Single();
		}

		public User GetLoggedUser(string token)
		{
			string username = Models.Security.TokenManager.GetUsername(token);
			return userDAO.Users
				.Where(u => u.Username.Equals(username))
				.Single();
		}

		public string Login(string username, string password)
		{
			User user = null;
			try
			{
				user = userDAO.Users
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

		public void Register(string username, string password, string repeatPassword, string carRegistrationNumber)
		{
			lock(userDAO.Users)
			{
				if (ExistUsernameOrRegistrationNumber(username, carRegistrationNumber))
				{
					throw new AlreadyExistThisUserException("Already exist user with username:  " + username
						+ ", or with car registration number:  " + carRegistrationNumber);
				}

				if (InvalidPasswords(password, repeatPassword))
				{
					throw new InvalidPasswordException("Invalid password or repeat password");
				}

				userDAO.AddUser(username, password, carRegistrationNumber);
			}
		
		}

		private bool InvalidPasswords(string password, string repeatPassword)
		{
			if (password == null || repeatPassword == null)
			{
				return true;
			}

			if (password.Length < 5)
			{
				return true;
			}

			if (!password.Equals(repeatPassword))
			{
				return true;
			}

			return false;
		}

		private bool ExistUsernameOrRegistrationNumber(string username, string registrationNumber)
		{
			return userDAO.Users
				.Exists(u => u.Username.Equals(username) || u.RegistrationNumber.Equals(registrationNumber));
		}
	}
}