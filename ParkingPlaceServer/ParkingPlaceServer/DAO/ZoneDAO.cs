using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DAO
{
	public class ZoneDAO
	{
        private List<Zone> zones = null;

        public List<Zone> getZones()
        {
            if (zones == null)
            {
                zones = loadZones();
                /*foreach (Zone zone in zones)
                {
                    foreach (ParkingPlace parkingPlace in zone.ParkingPlaces)
                    {
                        parkingPlace.Zone = zone;
                    }
                }*/
            }

            return zones;
        }

        private List<Zone> loadZones()
        {
            string filepath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Resources", "zones_with_parking_places.json");
            List<Zone> zones;
            string json;

            using (StreamReader r = new StreamReader(filepath))
            {
               json = r.ReadToEnd();
            }

            zones = JsonConvert.DeserializeObject<List<Zone>>(json);
            foreach(Zone zone in zones)
            {
                foreach(ParkingPlace parkingPlace in zone.ParkingPlaces)
                {
                    parkingPlace.Zone = zone;
                }
            }

            return zones;
        }
    }
}