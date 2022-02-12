package com.example.chatbot;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

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
    private int timer = 0;

    private void scrollDown() {
        chatsRV.scrollToPosition(chatsModelArrayList.size() - 1);
    }

    private boolean stringSearch(String toBeSearched, String str) {
        return str.contains(toBeSearched);
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


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializing components


        chatsRV = findViewById(R.id.idRVChats);

        chatsRV.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> chatsRV.scrollToPosition(Objects.requireNonNull(chatsRV.getAdapter()).getItemCount() - 1));


        userMsgEdt = findViewById(R.id.idEdtMessage);
        float value = userMsgEdt.getTextSize();
        int padding = (int) (value * 4f);

        FloatingActionButton sendMsgFab = findViewById(R.id.idFABSend);

        chatsModelArrayList = new ArrayList<>();
        chatRVAdapter = new ChatRVAdapter(chatsModelArrayList, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);

        chatsRV.setLayoutManager(manager);
        ((LinearLayoutManager) Objects.requireNonNull(chatsRV.getLayoutManager())).setStackFromEnd(true);
        chatsRV.setPadding(0, 0, 0, padding);
        chatsRV.setAdapter(chatRVAdapter);
        chatsRV.setNestedScrollingEnabled(false);

        chatsModelArrayList.add(new ChatsModel("Hi! Chefbot master at your service here! I'm here to help you find the best recipe in the world!", BOT_KEY));
        chatRVAdapter.notifyDataSetChanged();

        chatsModelArrayList.add(new ChatsModel("Please, write some ingredients separated by space for your recipe or you can tell if you're vegetarian to check for some vegetarian recipes!", BOT_KEY));
        chatRVAdapter.notifyDataSetChanged();

        sendMsgFab.setOnClickListener(v -> {
            if (userMsgEdt.getText().toString().isEmpty()) {

                Toast.makeText(MainActivity.this, "Please enter your message", Toast.LENGTH_SHORT).show();
                scrollDown();

                Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
                userMsgEdt.startAnimation(shake);

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

                        timer = 0;
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

                    timer = 0;
                    getNormalRecipes(ingredients, howMany);
                    break;


                case 2:
                    numberChoice = userMsgEdt.getText().toString();
                    userMsgEdt.setText("");

                    chatsModelArrayList.add(new ChatsModel(numberChoice, USER_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    scrollDown();

                    timer = 0;
                    pickRecipe(numberChoice);
                    break;

                case 3:
                    message = userMsgEdt.getText().toString();
                    scrollDown();
                    userMsgEdt.setText("");
                    chatsModelArrayList.add(new ChatsModel(message, USER_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    scrollDown();

                    timer = 0;
                    chatWithBot(message);
                    break;

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
                    msg.setChatBotReply(msg.getCnt());

                    if (isNumeric(msg.getCnt())) {

                        int exceptionNumber = Integer.parseInt(msg.getCnt());

                        if (exceptionNumber == 0) {

                            if (timer == 3) {

                                chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                                chatRVAdapter.notifyDataSetChanged();
                                scrollDown();
                                timer = 0;


                            } else {
                                timer++;

                            }


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

                        chatsModelArrayList.add(new ChatsModel("Please pick a recipe! Type the number of the recipe that you want!", BOT_KEY));
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

                chatsModelArrayList.add(new ChatsModel("Hi! Chefbot master at your service here! I'm here to help you find the best recipe in the world!", BOT_KEY));
                chatRVAdapter.notifyDataSetChanged();
                scrollDown();

                chatsModelArrayList.add(new ChatsModel("Please, write some ingredients separated by space for your recipe or you can tell if you're vegetarian to check for some vegetarian recipes!", BOT_KEY));
                chatRVAdapter.notifyDataSetChanged();
                scrollDown();

                counter = 0;

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
                    msg.setChatBotReply(msg.getCnt());

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

                            if (timer == 3) {

                                chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                                chatRVAdapter.notifyDataSetChanged();
                                scrollDown();
                                timer = 0;


                            } else {
                                timer++;

                            }

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

                chatsModelArrayList.add(new ChatsModel("Hi! Chefbot master at your service here! I'm here to help you find the best recipe in the world!", BOT_KEY));
                chatRVAdapter.notifyDataSetChanged();
                scrollDown();

                chatsModelArrayList.add(new ChatsModel("Please, write some ingredients separated by space for your recipe or you can tell if you're vegetarian to check for some vegetarian recipes!", BOT_KEY));
                chatRVAdapter.notifyDataSetChanged();
                scrollDown();

                counter = 0;
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
                        msg.setChatBotReply(msg.getCnt());


                        if (isNumeric(msg.getCnt())) {

                            int number = Integer.parseInt(msg.getCnt());

                            if (number == 0) {

                                if (timer == 3) {

                                    chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                                    chatRVAdapter.notifyDataSetChanged();
                                    scrollDown();
                                    timer = 0;


                                } else {
                                    timer++;

                                }

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

                            chatsModelArrayList.add(new ChatsModel("Now, you can ask me some questions for your recipe.\n" +
                                    "You can ask me how to cook it, what ingredients you need, what tools you need or maybe you're curious about recipe's nutrition!\n" +
                                    "Also, you can exit anytime by typing exit or quit. If message appears empty please try again", BOT_KEY));
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

                    chatsModelArrayList.add(new ChatsModel("Hi! Chefbot master at your service here! I'm here to help you find the best recipe in the world!", BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    scrollDown();

                    chatsModelArrayList.add(new ChatsModel("Please, write some ingredients separated by space for your recipe or you can tell if you're vegetarian to check for some vegetarian recipes!", BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                    scrollDown();

                    counter = 0;
                }
            });

        } else {
            chatsModelArrayList.add(new ChatsModel("Please enter a valid number between number 1 and " + numberOfRecipes, BOT_KEY));
            chatRVAdapter.notifyDataSetChanged();
            scrollDown();

        }


    }

    private void chatWithBot(String message) {

        String url = "https://masterchefbot.herokuapp.com/chat-with-bot?message=" + message;
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
                    msg.setChatBotReply(msg.getCnt());
                    if (isNumeric(msg.getCnt())) {

                        int errorNumber = Integer.parseInt(msg.getCnt());

                        if (errorNumber == 0) {

                            if (timer == 3) {

                                chatsModelArrayList.add(new ChatsModel("Hold on a second! I'm working on it ...", BOT_KEY));
                                chatRVAdapter.notifyDataSetChanged();
                                scrollDown();
                                timer = 0;


                            } else {
                                timer++;

                            }

                            chatWithBot(message);

                        } else if (errorNumber == 10) {

                            chatsModelArrayList.add(new ChatsModel("Recipe API is not available now! Try again later", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                        } else if (errorNumber == 1) {
                            chatsModelArrayList.add(new ChatsModel("Here you can start with new ingredients,please enter your ingredients separated by space or you can tell me if you are vegetarian!", BOT_KEY));
                            chatRVAdapter.notifyDataSetChanged();
                            scrollDown();

                            counter = 0;

                        } else if (errorNumber == -1) {


                            try {
                                chatsModelArrayList.add(new ChatsModel("Goodbye!", BOT_KEY));
                                chatRVAdapter.notifyDataSetChanged();
                                scrollDown();

                                sleep(5000);
                                finishAndRemoveTask();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


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


                        } else if (msg.getCnt().isEmpty()) {

                            chatWithBot(message);
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

                chatsModelArrayList.add(new ChatsModel("Hi! Chefbot master at your service here! I'm here to help you find the best recipe in the world!", BOT_KEY));
                chatRVAdapter.notifyDataSetChanged();
                scrollDown();

                chatsModelArrayList.add(new ChatsModel("Please, write some ingredients separated by space for your recipe or you can tell if you're vegetarian to check for some vegetarian recipes!", BOT_KEY));
                chatRVAdapter.notifyDataSetChanged();
                scrollDown();

                counter = 0;
            }
        });


    }


}