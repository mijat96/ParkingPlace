using System;

namespace ParkingPlaceServer.Models
{
	public class Punishment
	{
		public PunishmentType Type;
		public string Description;
		public DateTime DateTime;
		public int Duration; // hours
	
		
		public Punishment()
		{

		}

		public Punishment(PunishmentType type, string description, int duration)
		{
			Type = type;
			Description = description;
			DateTime = DateTime.Now;
			Duration = duration;
		}
	}
}