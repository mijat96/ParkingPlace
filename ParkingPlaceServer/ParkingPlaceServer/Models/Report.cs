using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Models
{
    public class Report
    {
        [Key]
        public int Id { get; set; }
        public string ImageUrl { get; set; }
        public string UsernameSubmitter { get; set; }
        public string Reason { get; set; }
        public int ParkingPlaceId { get; set; }
        public int ZoneId { get; set; }
        public DateTime DateTime { get; set; }
        public Report() { }
    }
}