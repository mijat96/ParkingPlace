using ParkingPlaceServer.Models;
using ParkingPlaceServer.Models.Security;
using ParkingPlaceServer.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace ParkingPlaceServer.Controllers
{
	public class ZonesController : ApiController
	{
		private IZonesService zonesService = ZonesService.Instance;

		// GET api/zones
		public HttpResponseMessage GetZones()
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			return Request.CreateResponse(HttpStatusCode.OK, zonesService.getZones());
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
