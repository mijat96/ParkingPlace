using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Models
{
    public class User
    {
        public string Username { get; set; }
        public string Password { get; set; }
        public string RegistrationNumber { get; set; }
        public int CurrentNumberOfReservationViolations { get; set; }
        public int TotalNumberOfReservationViolations { get; set; }
        public int CurrentNumberOfTakingViolations { get; set; }
        public int TotalNumberOfTakingViolations { get; set; }
        public List<FavoritePlace> FavoritePlaces { get; set; }
        public Reservation Reservation { get; set; }
        public PaidParkingPlace RegularPaidParkingPlace { get; set; }
        public List<PaidParkingPlace> PaidParkingPlacesForFavoritePlaces { get; set; }
        public List<Punishment> Punishments { get; set; }
        public Punishment ActivePunishment { get; set; }

        private static readonly int MAX_FAVORITE_PLACES = 3;
        private static int favoritePlaceIdGenerator = 0;


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
            FavoritePlaces = new List<FavoritePlace>();
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

        public long AddFavoritePlace(FavoritePlace favoritePlace)
        {
            if (FavoritePlaces.Count == MAX_FAVORITE_PLACES)
            {
                return -2;
            }

            favoritePlace.Id = favoritePlaceIdGenerator++;
            FavoritePlaces.Add(favoritePlace);
            return favoritePlace.Id;
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
            hashCode = hashCode * -1521134295 + EqualityComparer<List<FavoritePlace>>.Default.GetHashCode(FavoritePlaces);
            hashCode = hashCode * -1521134295 + EqualityComparer<Reservation>.Default.GetHashCode(Reservation);
            hashCode = hashCode * -1521134295 + EqualityComparer<PaidParkingPlace>.Default.GetHashCode(RegularPaidParkingPlace);
            hashCode = hashCode * -1521134295 + EqualityComparer<List<PaidParkingPlace>>.Default.GetHashCode(PaidParkingPlacesForFavoritePlaces);
            hashCode = hashCode * -1521134295 + EqualityComparer<List<Punishment>>.Default.GetHashCode(Punishments);
            hashCode = hashCode * -1521134295 + EqualityComparer<Punishment>.Default.GetHashCode(ActivePunishment);
            return hashCode;
        }

		public void LeavePaidParkingPlace(PaidParkingPlace paidParkingPlace)
		{
			if (RegularPaidParkingPlace.Equals(paidParkingPlace)) {
                RegularPaidParkingPlace = null;
            }

            PaidParkingPlacesForFavoritePlaces.Remove(paidParkingPlace);
		}

	}
}