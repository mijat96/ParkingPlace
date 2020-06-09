using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Resources
{
    public class ApplicationDbContext : DbContext
    {
        public DbSet<Report> Reports { get; set; }

        public ApplicationDbContext()
            : base("name=DefaultConnection")
        {
        }

        //public static ApplicationDbContext Create()
        //{
        //    return new ApplicationDbContext();
        //}
    }
}