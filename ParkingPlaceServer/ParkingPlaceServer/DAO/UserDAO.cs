using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.IO;

namespace ParkingPlaceServer.Services
{
	public class UserDAO
	{
        private List<User> users = null;

        public List<User> getUsers()
        {
            if (users == null)
            {
                users = loadUsers();
            }

            return users;
        }

        private List<User> loadUsers()
        {
            string filepath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Resources", "users.json");
            List<User> users;
            using (StreamReader r = new StreamReader(filepath))
            {
                string json = r.ReadToEnd();
                users = JsonConvert.DeserializeObject<List<User>>(json);
            }

            return users;
        }
    }
}