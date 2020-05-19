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

        public List<Zone> GetZones()
        {
            if (zones == null)
            {
                zones = LoadZones();
                SetNorthEastAndSouthWestForZones(zones);
            }

            return zones;
        }

        private void SetNorthEastAndSouthWestForZones(List<Zone> zones)
        {
            if (zones == null || (zones != null && zones.Count == 0))
            {
                return;
            }

            double maxNorth;
            double minSouth;
            double maxEast;
            double minWest;

            foreach (Zone zone in zones)
            {
                maxNorth = double.MinValue;
                minSouth = double.MaxValue;
                maxEast = double.MinValue;
                minWest = double.MaxValue;
                foreach (ParkingPlace parkingPlace in zone.ParkingPlaces)
                {
                    parkingPlace.Zone = zone;
                    
                    if (parkingPlace.Location.Latitude > maxNorth)
                    {
                        maxNorth = parkingPlace.Location.Latitude;
                    }
                    
                    if (parkingPlace.Location.Latitude < minSouth)
                    {
                        minSouth = parkingPlace.Location.Latitude;
                    }

                    if (parkingPlace.Location.Longitude > maxEast)
                    {
                        maxEast = parkingPlace.Location.Longitude;
                    }
                    
                    if (parkingPlace.Location.Longitude < minWest)
                    {
                        minWest = parkingPlace.Location.Longitude;
                    }
                }

                zone.NorthEast = new Location(maxNorth, maxEast);
                zone.SouthWest = new Location(minSouth, minWest);
            }
        }

        private List<Zone> LoadZones()
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