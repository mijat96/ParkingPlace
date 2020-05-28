using Microsoft.Ajax.Utilities;
using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Models;
using ParkingPlaceServer.Models.Security;
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
	public class UsersController : ApiController
	{

		private IUsersService usersService = UsersService.Instance;


		[Route("api/users/favorite-places/add-or-update")]
		[HttpPost]
		public async Task<HttpResponseMessage> AddOrUpdateFavoritePlace([FromBody] FavoritePlace value)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			User loggedUser = usersService.GetLoggedUser(token);
			
			long returnedId = usersService.AddOrUpdateFavoritePlace(loggedUser, value);
			if (returnedId == -2)
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest);
			}
			return Request.CreateResponse(HttpStatusCode.OK, returnedId);
		}

		[Route("api/users/favorite-places/{favoritePlaceId}/remove")]
		[HttpDelete]
		public async Task<HttpResponseMessage> RemoveFavoritePlace(long favoritePlaceId)
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			User loggedUser = usersService.GetLoggedUser(token);

			try
			{
				usersService.RemoveFavoritePlace(loggedUser, favoritePlaceId);
				return Request.CreateResponse(HttpStatusCode.OK);
			}
			catch
			{
				return Request.CreateResponse(HttpStatusCode.BadRequest);
			}
			
		}

		private string GetHeader(string key)
		{
			IEnumerable<string> keys = null;
			if (!Request.Headers.TryGetValues(key, out keys))
				return null;

			return keys.First();
		}

		[Route("api/users/favorite-places")]
		[HttpGet]
		public async Task<HttpResponseMessage> GetFavoritePlaces()
		{
			string token = GetHeader("token");
			if (token == null || (token != null && !TokenManager.ValidateToken(token)))
			{
				return Request.CreateResponse(HttpStatusCode.Unauthorized);
			}

			User loggedUser = usersService.GetLoggedUser(token);
			return Request.CreateResponse(HttpStatusCode.OK, loggedUser.FavoritePlaces);
		}
	}
}
