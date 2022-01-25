package com.example.chatbot;

import static java.lang.Thread.sleep;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatsRV;
    private EditText userMsgEdt;
    private final String BOT_KEY = "bot";
    private final String USER_KEY = "user";
    private ArrayList<ChatsModel> chatsModelArrayList;
    private ChatRVAdapter chatRVAdapter;
    private int counter = 0;
    private static final String TAG = "err";
    private String ingredients;
    private String howMany;
    private String numberChoice;
    private String message;
    private int numberOfRecipes;

    private void scrollDown() {
        chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
    }

    private boolean stringSearch(String toBeSearched, String str) {
        boolean isFound = str.indexOf(toBeSearched) != -1 ? true : false;
        return isFound;
    }

    private boolean isNumberInString(String str) {

        for (int i = 0; i <= str.length() - 1; i++) {

            if (isNumeric(String.valueOf(str.charAt(i)))) return true;

        }

        return false;
    }

    public boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void countRecipes(String aString) {
        int countBackSlash = 0;
        for (int i = 0; i < aString.length(); i++) {
            if (aString.charAt(i) == '\n') countBackSlash++;
        }
        numberOfRecipes = countBackSlash;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializing components


        chatsRV = findViewById(R.id.idRVChats);

        chatsRV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                chatsRV.scrollToPosition(chatsRV.getAdapter().getItemCount() - 1);
            }
        });


        userMsgEdt = findViewById(R.id.idEdtMessage);


        FloatingActionButton sendMsgFab = findViewById(R.id.idFABSend);

        chatsModelArrayList = new ArrayList<>();
        chatRVAdapter = new ChatRVAdapter(chatsModelArrayList, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);

        chatsRV.setLayoutManager(manager);
        ((LinearLayoutManager) chatsRV.getLayoutManager()).setStackFromEnd(true);
        chatsRV.setPadding(0, 0, 0, 200);
        chatsRV.setAdapter(chatRVAdapter);
        chatsRV.setNestedScrollingEnabled(false);

        chatsModelArrayList.add(new ChatsModel("Hi! Chefbot master at your service here! I'm here to help you find the best recipe in the world!", BOT_KEY));
        chatRVAdapter.notifyDataSetChanged();

        chatsModelArrayList.add(new ChatsModel("Please, write some ingredients separated by space for your recipe or you can tell if you're vegetarian to check for some vegetarian recipes!", BOT_KEY));
        chatRVAdapter.notifyDataSetChanged();

        sendMsgFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMsgEdt.getText().toString().isEmpty()) {

                    Toast.makeText(MainActivity.this, "Please enter your message", Toast.LENGTH_SHORT).show();
                    scrollDown();
                    return;

                }


                switch (counter) {
                    case 0:

                        ingredients = userMsgEdt.getText().toString();
                        userMsgEdt.setText("");

                        chatsModelArrayList.add(new ChatsModel(ingredients, USER_KEY));
                        chatRVAdapter.notifyDataSetChanged();
                        scrollDown();

                        if (stringSearch("vegetarian", ingredients)) {


                            getVegetarianRecipes();
                            break;

                        } else {


                            chatsModelArrayList.add(new ChatsModel("How many recipes do you want to see? Enter a number between 1 and 15", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 1;

                            break;

                        }

                    case 1:
                        howMany = userMsgEdt.getText().toString();
                        userMsgEdt.setText("");

                        chatsModelArrayList.add(new ChatsModel(howMany, USER_KEY));
                        chatRVAdapter.notifyDataSetChanged();
                        scrollDown();

                        getNormalRecipes(ingredients, howMany);
                        break;


                    case 2:
                        numberChoice = userMsgEdt.getText().toString();
                        userMsgEdt.setText("");

                        chatsModelArrayList.add(new ChatsModel(numberChoice, USER_KEY));
                        chatRVAdapter.notifyDataSetChanged();
                        scrollDown();

                        pickRecipe(numberChoice);
                        break;

                    case 3:
                        message = userMsgEdt.getText().toString();
                        scrollDown();
                        userMsgEdt.setText("");
                        chatsModelArrayList.add(new ChatsModel(message, USER_KEY));
                        chatRVAdapter.notifyDataSetChanged();
                        scrollDown();
                        chatWithBot(message);

                        break;

                }

            }
        });

    }

    private void getVegetarianRecipes() {

        String url = "https://masterchefbot.herokuapp.com/get-vegetarian-recipes";
        String BASE_URL = "https://masterchefbot.herokuapp.com/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);

        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if (response.isSuccessful()) {
                    MsgModel msg = response.body();
                    msg.setchatBotReply(msg.getCnt());

                    if (isNumeric(msg.getCnt())) {

                        int exceptionNumber = Integer.parseInt(msg.getCnt());

                        if (exceptionNumber == 0) {

                            chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            getVegetarianRecipes();

                        } else if (exceptionNumber == 10) {


                            chatsModelArrayList.add(new ChatsModel("Recipe API is not available now! Try again later", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                            return;

                        }

                    } else {
                        countRecipes(msg.getCnt());

                        chatsModelArrayList.add(new ChatsModel(msg.getCnt(), BOT_KEY));
                        chatRVAdapter.notifyDataSetChanged();
                        scrollDown();

                        counter = 2;

                        chatsModelArrayList.add(new ChatsModel("Please pick a recipe! Anything !", BOT_KEY));
                        chatRVAdapter.notifyDataSetChanged();
                        scrollDown();

                    }
                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {
                Log.e(TAG, String.valueOf(t));
                chatsModelArrayList.add(new ChatsModel("Error processing response", BOT_KEY));
                scrollDown();
            }
        });
    }

    private void getNormalRecipes(String ingredients, String number) {
        String url = "https://masterchefbot.herokuapp.com/get-recipe-by-user-ingredients?ingredients=" + ingredients + "&number=" + number;
        String BASE_URL = "https://masterchefbot.herokuapp.com/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);

        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if (response.isSuccessful()) {
                    MsgModel msg = response.body();
                    msg.setchatBotReply(msg.getCnt());

                    if (isNumeric(msg.getCnt())) {

                        int errorNumber = Integer.parseInt(msg.getCnt());
                        if (errorNumber == 1) {
                            chatsModelArrayList.add(new ChatsModel("Enter a valid number ...", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 1;

                            chatsModelArrayList.add(new ChatsModel("How many recipes do you want to see? Enter a number between 1 and 15", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                        } else if (errorNumber == 2) {
                            chatsModelArrayList.add(new ChatsModel("Inputs are invalid. Please start again...", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 0;

                            chatsModelArrayList.add(new ChatsModel("Please, write some ingredients separated by space for your recipe or you can tell if you're vegetarian to check for some vegetarian recipes!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                        }
                        if (errorNumber == 0) {

                            chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            getNormalRecipes(ingredients, number);

                        } else if (errorNumber == 10) {

                            chatsModelArrayList.add(new ChatsModel("Recipe API is not available now! Try again later", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                            return;

                        }

                    } else {

                        countRecipes(msg.getCnt());
                        if (numberOfRecipes < Integer.parseInt(number)) {
                            chatsModelArrayList.add(new ChatsModel("There are only " + numberOfRecipes + " recipes...", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            chatsModelArrayList.add(new ChatsModel(msg.getCnt(), BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 2;

                            chatsModelArrayList.add(new ChatsModel("Please enter the number of the recipe that you want!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                        } else {

                            chatsModelArrayList.add(new ChatsModel(msg.getCnt(), BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();

                            counter = 2;

                            chatsModelArrayList.add(new ChatsModel("Please enter the number of the recipe that you want!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                        }


                    }

                } else {
                    chatsModelArrayList.add(new ChatsModel("Please check the message", BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    scrollDown();


                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {
                Log.e(TAG, String.valueOf(t));
                chatsModelArrayList.add(new ChatsModel("Error processing response", BOT_KEY));
                scrollDown();
            }
        });
    }

    private void pickRecipe(String choice) {


        if (isNumberInString(choice) && Integer.parseInt(choice) <= numberOfRecipes && 1 <= Integer.parseInt(choice)) {


            String url = "https://masterchefbot.herokuapp.com/pick-recipe-number?number=" + choice;
            String BASE_URL = "https://masterchefbot.herokuapp.com/";

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
            Call<MsgModel> call = retrofitAPI.getMessage(url);

            call.enqueue(new Callback<MsgModel>() {
                @Override
                public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                    if (response.isSuccessful()) {
                        MsgModel msg = response.body();
                        msg.setchatBotReply(msg.getCnt());


                        if (isNumeric(msg.getCnt())) {

                            int number = Integer.parseInt(msg.getCnt());

                            if (number == 0) {

                                chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                                chatRVAdapter.notifyDataSetChanged();
                                scrollDown();

                                pickRecipe(numberChoice);

                            } else if (number == 10) {

                                chatsModelArrayList.add(new ChatsModel("Recipe API is not available now! Try again later", BOT_KEY));
                                chatRVAdapter.notifyDataSetChanged();
                                scrollDown();
                                return;

                            }

                        } else {
                            chatsModelArrayList.add(new ChatsModel(msg.getCnt(), BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 3;

                            chatsModelArrayList.add(new ChatsModel("Now, you can ask me some questions for your recipe.\n " +
                                    "You can ask me how to cook it, what ingredients you need, what tools you need or maybe you're curious about recipe's nutrition!\n" +
                                    "Also, you can exit anytime by typing exit or quit :) .", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                        }


                    } else {
                        chatsModelArrayList.add(new ChatsModel("Please check the message. There are only " + numberOfRecipes + ".", BOT_KEY));
                        chatRVAdapter.notifyDataSetChanged();
                        scrollDown();


                    }
                }

                @Override
                public void onFailure(Call<MsgModel> call, Throwable t) {
                    Log.e(TAG, String.valueOf(t));
                    chatsModelArrayList.add(new ChatsModel("Error processing response", BOT_KEY));
                    scrollDown();
                }
            });

        } else {
            chatsModelArrayList.add(new ChatsModel("Please enter a valid number between number 1 and " + numberOfRecipes, BOT_KEY));
            chatRVAdapter.notifyDataSetChanged();
            scrollDown();

        }


    }

    private void chatWithBot(String message) {

        String url = "https://masterchefbot.herokuapp.com/chatWithBot-with-bot?message=" + message;
        String BASE_URL = "https://masterchefbot.herokuapp.com/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);

        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if (response.isSuccessful()) {
                    MsgModel msg = response.body();
                    msg.setchatBotReply(msg.getCnt());
                    if (isNumeric(msg.getCnt())) {

                        int errorNumber = Integer.parseInt(msg.getCnt());

                        if (errorNumber == 0) {

                            chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            chatWithBot(message);

                        } else if (errorNumber == 10) {

                            chatsModelArrayList.add(new ChatsModel("Recipe API is not available now! Try again later", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                        } else if (errorNumber == 1) {
                            chatsModelArrayList.add(new ChatsModel("Here you can start with new ingredients!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 0;

                            chatsModelArrayList.add(new ChatsModel("Enter your ingredients separated by space!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();
                        } else if (errorNumber == -1) {

                            chatsModelArrayList.add(new ChatsModel("Goodbye!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            try {
                                sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            return;
                        }

                    } else {

                        if (stringSearch("Welcome (back) to the overview:", msg.getCnt())) {
                            chatsModelArrayList.add(new ChatsModel(msg.getCnt(), BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 2;

                            chatsModelArrayList.add(new ChatsModel("Please enter the number of the recipe that you want!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();


                        } else {
                            chatsModelArrayList.add(new ChatsModel(msg.getCnt(), BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 3;

                            chatsModelArrayList.add(new ChatsModel("Ask me questions about the chosen recipe.\n " +
                                    "You can ask me about the ingredients, the cooking steps, the recipe's nutrition or simply ask to go back or make a new search.\n", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                        }


                    }

                } else {
                    chatsModelArrayList.add(new ChatsModel("Please check the message", BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    scrollDown();


                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {
                Log.e(TAG, String.valueOf(t));
                chatsModelArrayList.add(new ChatsModel("Error processing response", BOT_KEY));
                scrollDown();
            }
        });


    }


}