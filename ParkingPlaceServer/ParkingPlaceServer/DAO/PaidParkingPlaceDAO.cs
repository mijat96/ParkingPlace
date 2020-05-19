using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.IO;

namespace ParkingPlaceServer.Services
{
	public class PaidParkingPlaceDAO
	{
        private List<PaidParkingPlace> paidParkingPlaces = null;

        public List<PaidParkingPlace> GetPaidParkingPlaces()
        {
            if (paidParkingPlaces == null)
            {
                paidParkingPlaces = new List<PaidParkingPlace>();
            }

            return paidParkingPlaces;
        }

        public void AddPaidParkingPlace(PaidParkingPlace paidParkingPlace)
        {
            paidParkingPlaces.Add(paidParkingPlace);
        }
    }
}