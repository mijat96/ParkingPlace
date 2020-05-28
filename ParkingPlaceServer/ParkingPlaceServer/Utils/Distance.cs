using System;
using System.Collections.Generic;
using System.Device.Location;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Utils
{
	public class Distance
	{
		/*
		  return value in meters
			 */
		public static double computeDistance(double firstPointLatitude, double firstPointLongitude,
											double secondPointLatitude, double secondPointLongitude)
		{
			GeoCoordinate firstPoint = new GeoCoordinate(firstPointLatitude, firstPointLongitude);
			GeoCoordinate secondPoint = new GeoCoordinate(secondPointLatitude, secondPointLongitude);

			return firstPoint.GetDistanceTo(secondPoint);
		}
	}
}