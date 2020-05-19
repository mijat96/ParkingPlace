using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Helpers;
using System.Web.Http;

namespace ParkingPlaceServer.Controllers
{
	public class AuthenticationController : ApiController
	{

		private IUsersService usersService = UsersService.Instance;


		[Route("api/authentication/login")]
		public async Task<HttpResponseMessage> PostLogin([FromBody] LoginDTO value)
		{
			try
			{
				string token = usersService.Login(value.Username, value.Password);
				return Request.CreateResponse(HttpStatusCode.OK, new TokenDTO(token));
			}
			catch(InvalidUsernameOrPasswordException e)
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized, e.Message);
			}

			
		}

		[Route("api/authentication/register")]
		public async Task<HttpResponseMessage> PostRegister([FromBody] RegistrationDTO value)
		{
			try
			{
				usersService.Register(value.Username, value.Password, value.RepeatPassword, value.CarRegistrationNumber);
				return Request.CreateResponse(HttpStatusCode.OK);
			}
			catch (Exception e)
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest, e.Message);
			}


		}
	}
}
