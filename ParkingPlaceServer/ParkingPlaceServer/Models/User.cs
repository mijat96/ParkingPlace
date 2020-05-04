using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Models
{
    public class User
    {
        public string Username;
        public string Password;
        public string RegistrationNumber;
        public int CurrentNumberOfReservationViolations;
        public int TotalNumberOfReservationViolations;
        public int CurrentNumberOfTakingViolations;
        public int TotalNumberOfTakingViolations;
        public List<Location> FavoritePlaces;
        public Reservation Reservation;
        public PaidParkingPlace RegularPaidParkingPlace;
        public List<PaidParkingPlace> PaidParkingPlacesForFavoritePlaces;
        public List<Punishment> Punishments;
        public Punishment ActivePunishment;


        public User()
        {

        }

        public User(String username, String password, String registrationNumber)
        {
            Username = username;
            Password = password;
            RegistrationNumber = registrationNumber;
            CurrentNumberOfReservationViolations = 0;
            TotalNumberOfReservationViolations = 0;
            CurrentNumberOfTakingViolations = 0;
            TotalNumberOfTakingViolations = 0;
            FavoritePlaces = new List<Location>();
            Reservation = null;
            RegularPaidParkingPlace = null;
            PaidParkingPlacesForFavoritePlaces = new List<PaidParkingPlace>();
            Punishments = new List<Punishment>();
            ActivePunishment = null;
        }

        public void AddViolation(bool reservation)
        {
            if (reservation)
            {
                CurrentNumberOfReservationViolations++;
                TotalNumberOfReservationViolations++;
            }
            else
            {
                CurrentNumberOfTakingViolations++;
                TotalNumberOfTakingViolations++;
            }

        }

    }
}