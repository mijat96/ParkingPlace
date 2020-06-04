using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ReservationAndPaidParkingPlacesDTO
	{
		[JsonProperty("reservation")]
		public ReservationDTO Reservation { get; set; }

		[JsonProperty("regularPaidParkingPlace")]
		public PaidParkingPlaceDTO RegularPaidParkingPlace { get; set; }

		[JsonProperty("paidParkingPlacesForFavoritePlaces")]
		public List<PaidParkingPlaceDTO> PaidParkingPlacesForFavoritePlaces { get; set; }
		

		public ReservationAndPaidParkingPlacesDTO()
		{

		}

		public ReservationAndPaidParkingPlacesDTO(Reservation r, PaidParkingPlace rp, List<PaidParkingPlace> ps)
		{
			if (r == null)
			{
				Reservation = null;
			}
			else
			{
				Reservation = new ReservationDTO(r);
			}

			if (rp == null)
			{
				RegularPaidParkingPlace = null;
			}
			else
			{
				RegularPaidParkingPlace = new PaidParkingPlaceDTO(rp);
			}

			if (ps == null || (ps != null && ps.Count == 0))
			{
				PaidParkingPlacesForFavoritePlaces = new List<PaidParkingPlaceDTO>();
			}
			else
			{
				PaidParkingPlacesForFavoritePlaces = ps.Select(p => new PaidParkingPlaceDTO(p)).ToList();
			}
			
		}

	}
}