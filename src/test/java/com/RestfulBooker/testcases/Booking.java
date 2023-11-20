package com.RestfulBooker.testcases;
import com.RestfulBooker.pojo.createtokenPojo;
import com.sun.xml.bind.v2.model.core.ID;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javafx.scene.layout.Priority;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import java.io.File;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class  Booking {

    @Test(priority = 1)
    public void GetAllBookingId(){
        given()
            .baseUri("https://restful-booker.herokuapp.com")
        .when()
            .get("booking")
        .then()
            .log().body()
            .assertThat().statusCode(200) //check Status Code
            .assertThat().body("[0]",hasKey("bookingid")) //check that response contains ids
            .assertThat().header("Content-Type",equalTo("application/json; charset=utf-8"));
    }
    @Test(priority = 2)
    public void CreateNewBooking(ITestContext context){
        File body= new File("src/test/resources/createbooking.json");

        int ID=
            given()
                .baseUri("https://restful-booker.herokuapp.com")
                .body(body)
                .contentType(ContentType.JSON)
            .when()
                .post("booking")
            .then().extract().response().path("bookingid");
        context.setAttribute("ID",ID);
        System.out.println("Booking has been created Successfully");
        System.out.println("Booking ID is: "+ID);
    }
    @Test(priority = 3, dependsOnMethods="CreateNewBooking")
    public void VerifyCreatingProcessByGettingBookingInfo(ITestContext context){
        int ID =(int) context.getAttribute("ID");
        System.out.println("Booking Info: ");
        given()
            .baseUri("https://restful-booker.herokuapp.com")
        .when()
            .get("booking/"+ID)
        .then()
            .log().body()
            .assertThat().statusCode(200)
            .assertThat().body("",hasKey("firstname"),"",hasKey("lastname")
                        ,"",hasKey("totalprice"),"",hasKey("depositpaid"))
            .assertThat().header("Content-Type",equalTo("application/json; charset=utf-8"));
    }
    @Test(priority = 4)
    public  void CreateNewToken(ITestContext context){
        createtokenPojo body =new createtokenPojo();
            body.setUsername("admin");
            body.setPassword("password123");
        String TOKEN =
            given()
                .baseUri("https://restful-booker.herokuapp.com")
                .body(body)
                .contentType(ContentType.JSON)
            .when()
                .post("auth")
            .then().extract().jsonPath().get("token");
       context.setAttribute("TOKEN",TOKEN);
       System.out.println("Token is: "+TOKEN);
    }
    @Test(priority = 5, dependsOnMethods ={"CreateNewToken","CreateNewBooking"})
    public void UpdateBookingInfo(ITestContext context){
        int ID =(int) context.getAttribute("ID");   //ID from new booking
        String TOKEN =(String) context.getAttribute("TOKEN");    //token from new Token
        File body= new File("src/test/resources/updatebooking.json");
        System.out.println("Booking has been updated Successfully");
        System.out.println("Booking Info(after Updating): ");
        given()
                .baseUri("https://restful-booker.herokuapp.com")
                .contentType(ContentType.JSON)
                .cookie("token",TOKEN)
                .body(body)
        .when()
                .put("booking/"+ID)
        .then()
                .log().body()
                .assertThat().statusCode(200)
                .assertThat().body("",hasKey("firstname"),"",hasKey("lastname")
                        ,"",hasKey("totalprice"),"",hasKey("depositpaid"))
                .assertThat().header("Content-Type",equalTo("application/json; charset=utf-8"));
    }
    @Test(priority = 6)
    public void TryToDeleteBookingWithoutToken(){
        given()
                .baseUri("https://restful-booker.herokuapp.com")
        .when()
                .delete("booking/1")
        .then()
                .log().body()
                .assertThat().statusCode(403);
        System.out.println("You are not allowed to delete");
    }
    @Test(priority = 7, dependsOnMethods = {"CreateNewToken","CreateNewBooking"})
    public void DeleteBookingWithToken(ITestContext context){
        int ID =(int) context.getAttribute("ID"); //ID from Create Booking
        String TOKEN =(String) context.getAttribute("TOKEN"); //Token from Create Token
        given()
                .baseUri("https://restful-booker.herokuapp.com")
                .contentType(ContentType.JSON)
                .cookie("token",TOKEN)
        .when()
                .delete("booking/"+ID)
        .then()
                .assertThat().statusCode(201);
        System.out.println("Booking has been deleted Successfully");
    }
    @Test(priority = 8, dependsOnMethods={"CreateNewToken","CreateNewBooking","DeleteBookingWithToken"})
    public void VerifyThatBookingIsDeleted(ITestContext context){
        int ID =(int) context.getAttribute("ID");
        given()
                .baseUri("https://restful-booker.herokuapp.com")
        .when()
                .get("booking/"+ID)
        .then()
                .log().body()
                .assertThat().statusCode(404);
        System.out.println("It appears that the user you are trying to reach has been deleted or does not exist");
    }

}
