using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
    public class ReportDTO : Dto
    {
        public Bitmap image { get; set; }

        public ReportDTO()
        {

        }
    }
}