using System;
using System.Runtime.Serialization;

namespace ParkingPlaceServer.Services
{
	[Serializable]
	public class AlreadyExistThisUserException : Exception
	{
		public AlreadyExistThisUserException()
		{
		}

		public AlreadyExistThisUserException(string message) : base(message)
		{
		}

		public AlreadyExistThisUserException(string message, Exception innerException) : base(message, innerException)
		{
		}

		protected AlreadyExistThisUserException(SerializationInfo info, StreamingContext context) : base(info, context)
		{
		}
	}
}