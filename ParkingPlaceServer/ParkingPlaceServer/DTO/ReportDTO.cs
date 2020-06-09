using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Globalization;
using System.IdentityModel.Protocols.WSTrust;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
    public class ReportDTO
    {
        public string reason { get; set; }
        public int parkingPlaceId { get; set; }
        public int zoneId { get; set; }
        public string dateTime { get; set; }
        public string address { get; set; }
        public string status {get; set;}

        private static readonly string formatSpecifier = "G";
        private static readonly CultureInfo culture = CultureInfo.CreateSpecificCulture("de-DE");

        public ReportDTO(string reason, int parkingPlaceId, int zoneId, DateTime dateTime, string address, string status)
        {
            this.dateTime = dateTime.ToString(formatSpecifier, culture);
            this.address = address;
            this.zoneId = zoneId;
            this.reason = reason;
            this.parkingPlaceId = parkingPlaceId;
            this.status = status;
        }

        public ReportDTO(Report r, string address)
        {
            this.reason = r.Reason;
            this.parkingPlaceId = r.ParkingPlaceId;
            this.zoneId = r.ZoneId;
            this.dateTime = r.DateTime.ToString(formatSpecifier, culture);
            this.address = address;
            this.status = "odobreno";
        }

        public ReportDTO()
        {

        }
    }
}