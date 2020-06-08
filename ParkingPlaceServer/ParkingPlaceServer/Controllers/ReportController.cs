using ParkingPlaceServer.DTO;
using System;
using System.Collections.Generic;
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
        // POST: Report
        [Route("api/reports/sendReport")]
        [HttpPost]
        public async Task<HttpResponseMessage> reportPackedCar()
        {
            string fileName = "";
            var httpRequest = HttpContext.Current.Request;


            if (httpRequest.Files.Count > 0)
            {
                foreach (string file in httpRequest.Files)
                {
                    var postedFile = httpRequest.Files[file];
                    fileName = postedFile.FileName;
                }
            }

            //iscupati ostale parametre
            if (httpRequest.Form.Count > 0)
            {
                foreach(string form in httpRequest.Form)
                {
                    var parametars = httpRequest.Form[form];
                    
                }
            }
            //Sacuvati u bazu sliku
            if (fileName.Equals(""))
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest);
            }
            else
            {
                return Request.CreateResponse(HttpStatusCode.OK);
            }
        }
    }
}