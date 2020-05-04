using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace ParkingPlaceServer.Controllers
{
	public class AuthenticationController : ApiController
	{

		private IUsersService usersService = UsersService.Instance;


		[Route("api/authentication/login")]
		public HttpResponseMessage Post([FromBody] LoginDTO value)
		{
			try
			{
				string token = usersService.login(value.Username, value.Password);
				return Request.CreateResponse(HttpStatusCode.OK, new TokenDTO(token));
			}
			catch(InvalidUsernameOrPasswordException e)
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized, e.Message);
			}

			
		}


	}
}
