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

        public override bool Equals(object obj)
        {
            return obj is User user &&
                   Username == user.Username;
        }

        public override int GetHashCode()
        {
            int hashCode = -808327111;
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(Username);
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(Password);
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(RegistrationNumber);
            hashCode = hashCode * -1521134295 + CurrentNumberOfReservationViolations.GetHashCode();
            hashCode = hashCode * -1521134295 + TotalNumberOfReservationViolations.GetHashCode();
            hashCode = hashCode * -1521134295 + CurrentNumberOfTakingViolations.GetHashCode();
            hashCode = hashCode * -1521134295 + TotalNumberOfTakingViolations.GetHashCode();
            hashCode = hashCode * -1521134295 + EqualityComparer<List<Location>>.Default.GetHashCode(FavoritePlaces);
            hashCode = hashCode * -1521134295 + EqualityComparer<Reservation>.Default.GetHashCode(Reservation);
            hashCode = hashCode * -1521134295 + EqualityComparer<PaidParkingPlace>.Default.GetHashCode(RegularPaidParkingPlace);
            hashCode = hashCode * -1521134295 + EqualityComparer<List<PaidParkingPlace>>.Default.GetHashCode(PaidParkingPlacesForFavoritePlaces);
            hashCode = hashCode * -1521134295 + EqualityComparer<List<Punishment>>.Default.GetHashCode(Punishments);
            hashCode = hashCode * -1521134295 + EqualityComparer<Punishment>.Default.GetHashCode(ActivePunishment);
            return hashCode;
        }
    }
}