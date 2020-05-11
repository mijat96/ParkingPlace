using ParkingPlaceServer.Models;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Comparers
{

    public class ParkingPlaceStatusAndVersionComparer : IComparer<ParkingPlaceStatusAndVersion>
    {
        public int Compare(ParkingPlaceStatusAndVersion a, ParkingPlaceStatusAndVersion b)
        {
            if (a.Version == b.Version)
            {
                return 0;
            }
            else if (a.Version < b.Version)
            {
                return -1;
            }
            else
            {
                return 1;
            }
           
        }
   
    }
}