package com.travelplanner;

import com.travelplanner.entity.*;
import com.travelplanner.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import java.util.*;

@SpringBootApplication
public class TravelPlannerApplication {
  public static void main(String[] args){SpringApplication.run(TravelPlannerApplication.class,args);}

  @Bean @Order(1) CommandLineRunner seed(
      UserRepository users,
      DestinationRepository destinations,
      PasswordEncoder encoder,
      @Value("${app.admin.email:}") String adminEmail,
      @Value("${app.admin.name:Travel Planner Admin}") String adminName,
      @Value("${app.admin.password:}") String adminPassword
  ){return args -> {
    if (StringUtils.hasText(adminEmail) && StringUtils.hasText(adminPassword)) {
      String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
      if (users.findByEmailIgnoreCase(normalizedEmail).isEmpty()) {
        User admin = new User();
        admin.setName(adminName.trim());
        admin.setEmail(normalizedEmail);
        admin.setPassword(encoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        users.save(admin);
      }
    }
    {
      List<Destination> d=List.of(
        dest("Hyderabad","Telangana","City","Culture, Food, Heritage",5500,2,"Oct-Feb","Road / Train / Flight","Families, Foodies, History lovers","Charminar; Golconda Fort; Chowmahalla Palace; Hussain Sagar","A lively Deccan city combining heritage monuments, biryani, bazaars and modern cafés.","https://images.unsplash.com/photo-1597040663342-45b6af1b7b5b?auto=format&fit=crop&w=1200&q=85"),
        dest("Goa","Goa","Beach","Beaches, Nightlife, Water Sports",9000,4,"Nov-Feb","Flight / Train / Road","Couples, Friends, Beach lovers","Baga & Palolem beaches; Fort Aguada; Old Goa; water sports","A relaxed coastal escape with beaches, Portuguese heritage, seafood and lively evenings.","https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1200&q=85"),
        dest("Jaipur","Rajasthan","Heritage","History, Architecture, Shopping",8000,3,"Oct-Mar","Flight / Train / Road","Families, Culture lovers, Photographers","Amber Fort; City Palace; Hawa Mahal; Jantar Mantar; local bazaars","The Pink City is ideal for forts, royal architecture, crafts and traditional Rajasthani food.","https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1200&q=85"),
        dest("Munnar","Kerala","Nature","Tea, Hills, Wildlife",7500,3,"Sep-May","Flight + Road / Train + Road","Couples, Nature lovers","Tea estates; Eravikulam National Park; Top Station; waterfalls","Misty tea-covered hills, cool weather and scenic drives make Munnar a peaceful mountain retreat.","https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=1200&q=85"),
        dest("Manali","Himachal Pradesh","Mountain","Snow, Adventure, Trekking",12000,4,"Mar-Jun / Dec-Feb","Flight + Road / Bus","Friends, Couples, Adventure lovers","Solang Valley; Rohtang region; Old Manali; Hidimba Temple","A Himalayan base for snow experiences, valleys, cafés and outdoor adventure.","https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=1200&q=85"),
        dest("Varanasi","Uttar Pradesh","Spiritual","Heritage, Spirituality, Food",6500,2,"Oct-Mar","Flight / Train","Culture lovers, Pilgrims, Photographers","Ganga Aarti; Dashashwamedh Ghat; Sarnath; old-city lanes","Experience the ancient ghats, evening Ganga Aarti, temples and distinctive North Indian cuisine.","https://images.unsplash.com/photo-1561361513-2d000a50f0dc?auto=format&fit=crop&w=1200&q=85"),
        dest("Udaipur","Rajasthan","Romantic","Lakes, Palaces, Culture",9000,3,"Oct-Mar","Flight / Train / Road","Couples, Families, Photographers","Lake Pichola; City Palace; Sajjangarh; Bagore Ki Haveli","A lakeside city of palaces, sunset boat rides and elegant old-world streets.","https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1200&q=85"),
        dest("Kochi","Kerala","Coastal","Backwaters, Food, Heritage",7000,3,"Oct-Mar","Flight / Train","Families, Foodies, Culture lovers","Fort Kochi; Chinese fishing nets; Mattancherry; Kerala cuisine","A gateway to Kerala with colonial streets, art, seafood and easy access to backwater experiences.","https://images.unsplash.com/photo-1593693397690-362cb9666fc2?auto=format&fit=crop&w=1200&q=85"),
        dest("Rishikesh","Uttarakhand","Adventure","Rafting, Yoga, Nature",7000,3,"Sep-Nov / Feb-Jun","Flight + Road / Train + Road","Friends, Adventure lovers, Wellness seekers","River rafting; Laxman Jhula area; Ganga Aarti; yoga retreats","A riverside adventure and wellness destination surrounded by Himalayan foothills.","https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=1200&q=85"),
        dest("Ladakh","Ladakh","Mountain","Road Trips, Lakes, Monasteries",18000,6,"May-Sep","Flight / Road Trip","Adventure lovers, Photographers","Pangong Lake; Nubra Valley; monasteries; Khardung region","High-altitude landscapes, dramatic roads and monasteries create a memorable Himalayan road-trip experience.","https://images.unsplash.com/photo-1605649487212-47bdab064df7?auto=format&fit=crop&w=1200&q=85"),
        dest("Andaman","Andaman & Nicobar","Island","Beaches, Diving, Nature",16000,5,"Oct-May","Flight","Couples, Families, Divers","Radhanagar Beach; snorkeling; scuba diving; island cruises","Crystal-clear water, coral reefs and island beaches make this a premium tropical escape.","https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=85"),
        dest("Ooty","Tamil Nadu","Hill Station","Tea, Nature, Family",6500,3,"Oct-Jun","Train / Road / Flight + Road","Families, Couples, Nature lovers","Ooty Lake; Botanical Garden; toy train; tea estates","A classic Nilgiri hill station with cool weather, gardens, tea estates and scenic rail journeys.","https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=85"),
        dest("Agra","Uttar Pradesh","Heritage","Monuments, History, Photography",5000,2,"Oct-Mar","Train / Road / Flight","Families, History lovers","Taj Mahal; Agra Fort; Mehtab Bagh; local crafts","A compact heritage trip centered on the Taj Mahal and Mughal-era monuments.","https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=1200&q=85"),
        dest("Amritsar","Punjab","Heritage","Food, History, Culture",5500,2,"Oct-Mar","Flight / Train","Families, Foodies, History lovers","Golden Temple; Jallianwala Bagh; Wagah-Attari border","A culturally rich city known for the Golden Temple, Punjabi food and important modern history.","https://images.unsplash.com/photo-1609948543911-7f3c5e8d9f6a?auto=format&fit=crop&w=1200&q=85"),
        dest("Kanyakumari","Tamil Nadu","Coastal","Sunrise, Temples, Sea",5500,2,"Oct-Mar","Train / Flight + Road","Families, Couples, Photographers","Vivekananda Rock; Thiruvalluvar statue; sunrise point","India's southern tip offers ocean views, memorable sunrises and a blend of coastal culture.","https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=1200&q=85")
      );
      List<Destination> missing = d.stream()
          .filter(candidate -> destinations.findByNameIgnoreCase(candidate.getName()).isEmpty())
          .toList();
      if (!missing.isEmpty()) {
        destinations.saveAll(missing);
      }
    }
  };}

  private static Destination dest(String name,String state,String type,String interests,double budget,int days,String best,String travel,String ideal,String highlights,String description,String image){
    Destination d=new Destination();d.setName(name);d.setLocation(state+", India");d.setState(state);d.setType(type);d.setInterests(interests);d.setAverageBudget(budget);d.setRecommendedDays(days);d.setBestTime(best);d.setTravelFromHyderabad(travel);d.setIdealFor(ideal);d.setHighlights(highlights);d.setDescription(description);d.setImageUrl(image);return d;
  }
}
