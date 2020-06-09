using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Models;
using ParkingPlaceServer.Models.Security;
using ParkingPlaceServer.Resources;
using ParkingPlaceServer.Services;
using System;
using System.Collections.Generic;
using System.Data.Common;
using System.Data.Entity;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.Remoting.Messaging;
using System.Threading.Tasks;
using System.Web;
using System.Web.Http;

namespace ParkingPlaceServer.Controllers
{
    public class ReportController : ApiController
    {
        private IUsersService usersService = UsersService.Instance;
        private IZonesService zonesService = ZonesService.Instance;

        // POST: Report
        [Route("api/reports/sendReport")]
        [HttpPost]
        public IHttpActionResult reportPackedCar()
        {
            string token = GetHeader("token");
            if (token == null || (token != null && !TokenManager.ValidateToken(token)))
            {
                return Unauthorized();
                //return Request.CreateResponse(HttpStatusCode.Unauthorized);
            }

            User loggedUser = usersService.GetLoggedUser(token);

            var filePath = HttpContext.Current.Server.MapPath("~/ReportImages/");
            string fileName = "";
            Report report = new Report();
            var httpRequest = HttpContext.Current.Request;
            HttpPostedFile postedFile = null;

            if (httpRequest.Files.Count > 0)
            {
                foreach (string file in httpRequest.Files)
                {
                    postedFile = httpRequest.Files[file];
                    fileName = postedFile.FileName;
                    //postedFile.SaveAs(filePath + fileName);

                }
            }

            //iscupati ostale parametre
            if (httpRequest.Form.Count > 0)
            {
                foreach(string form in httpRequest.Form)
                {
                    if (form.Equals("parkingPlaceId"))
                    {
                        report.ParkingPlaceId = Int32.Parse(httpRequest.Form[form]);

                    }
                    else if (form.Equals("zoneId"))
                    {
                        report.ZoneId = Int32.Parse(httpRequest.Form[form]);
                    }
                    else if (form.Equals("reason"))
                    {
                        report.Reason = httpRequest.Form[form];
                    }
                    
                }
            }

            report.UsernameSubmitter = loggedUser.Username;
            report.ImageUrl = filePath + fileName;
            report.DateTime = DateTime.Now;

            using (var db = new ApplicationDbContext())
            {
                db.Reports.Add(report);
                db.SaveChanges();
            }

            postedFile.SaveAs(Path.Combine(filePath + report.ParkingPlaceId + "Report" + report.Id + ".jpg"));
            if (fileName.Equals(""))
            {
                return BadRequest();
                //return Request.CreateResponse(HttpStatusCode.BadRequest);
            }
            else
            {
                return Ok();
                //return Request.CreateResponse(HttpStatusCode.OK);
            }
        }

        [Route("api/reports/getReports")]
        [HttpGet]
        public async Task<HttpResponseMessage> getReports()
        {
            string token = GetHeader("token");
            if (token == null || (token != null && !TokenManager.ValidateToken(token)))
            {
                return Request.CreateResponse(HttpStatusCode.Unauthorized);
            }

            User loggedUser = usersService.GetLoggedUser(token);
            List<ReportDTO> allUserReports = new List<ReportDTO>();
            using (var db = new ApplicationDbContext())
            {
                List<Report> listReports = db.Reports.ToList();
                //allUserReports =  db.Reports.Where(r => r.UsernameSubmitter.Equals(loggedUser.Username))
                //                                          .Select(r => new ReportDTO(r, zonesService.GetZone(r.ZoneId)
                //                                                                                    .GetParkingPlace(r.ParkingPlaceId)
                //                                                                                    .Location.Address))
                //                                           .ToList();
                foreach (Report r in listReports)
                {
                    if (r.UsernameSubmitter.Equals(loggedUser.Username))
                    {
                        string address = zonesService.GetZone(r.ZoneId).GetParkingPlace(r.ParkingPlaceId).Location.Address;
                        allUserReports.Add(new ReportDTO(r, address));
                    }

                }
            }

            if(allUserReports != null)
            {
                return Request.CreateResponse(HttpStatusCode.OK, allUserReports);
            }

            return Request.CreateResponse(HttpStatusCode.NotFound);

        }

        private string GetHeader(string key)
        {
            IEnumerable<string> keys = null;
            if (!Request.Headers.TryGetValues(key, out keys))
                return null;

            return keys.First();
        }

    }
}