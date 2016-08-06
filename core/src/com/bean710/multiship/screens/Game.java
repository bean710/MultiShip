package com.bean710.multiship.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.bean710.multiship.MultiShip;
import com.bean710.multiship.Starship;
import com.bean710.multiship.dontshowonstream.IP;
import com.bean710.multiship.Bullet;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Game implements Screen {

	final MultiShip game;

	SpriteBatch batch;
	Starship player;
	Texture playerShip;
	Texture friendlyShip;
	HashMap<String, Starship> friendlyPlayers;
	ArrayList<Bullet> bullets;

	Vector2 prev;

	private String id;
	private String tempId;

	private String ip = IP.getIp();
	private Socket socket;

	private long lastShot = 0;

	public Game(final MultiShip gam) {

		this.game = gam;

		batch = new SpriteBatch();
		playerShip = new Texture(Gdx.files.internal("playerShip2.png"));
		friendlyShip = new Texture(Gdx.files.internal("playerShip.png"));
		friendlyPlayers = new HashMap<String, Starship>();
		bullets = new ArrayList<Bullet>();
		connectSocket();
		configureSocketEvents();

	}

	public void handleInput(float dt) {
		if (player != null) {
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				player.setPosition(player.getX() + (-200 * dt), player.getY());
			}
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				player.setPosition(player.getX() + (+200 * dt), player.getY());
			}
			if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
				if (System.currentTimeMillis() - lastShot > 1000) {
					lastShot = System.currentTimeMillis();
					shoot();
				}
			}
		}
	}

	public void shoot() {
		Bullet tempBull = new Bullet(friendlyShip, 10);
		tempBull.setPosition(player.getX(), player.getY());
		bullets.add(tempBull);
	}

	@Override
	public void render(float delta) {

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput(Gdx.graphics.getDeltaTime());

		batch.begin();

		Iterator<Bullet> i = bullets.iterator();
		if (!bullets.isEmpty()) {
			Gdx.app.log("Bullet", "not emptey");
			while (i.hasNext()) {
				Bullet bullet = i.next();
				if (bullet.getY() > 600) {
					i.remove();
				} else {
					bullet.setPosition(bullet.getX(), bullet.getY() + 10);
					Gdx.app.log("Bullet", bullets.size() + "");
					bullet.draw(batch);
				}
			}
		}

		if (player != null) {
			player.draw(batch);
			if (player.hasMoved()) {
				JSONObject data = new JSONObject();
				try {
					data.put("id", id);
					data.put("x", Double.parseDouble(Float.toString(player.getX())));
					data.put("y", Double.parseDouble(Float.toString(player.getY())));
					// Gdx.app.log("Debug", data.toString());
				} catch (Exception e) {

				}

				socket.emit("update", data);

			}
		}

		for (HashMap.Entry<String, Starship> entry : friendlyPlayers.entrySet()) {
			entry.getValue().draw(batch);
		}

		batch.end();

		Gdx.graphics.setTitle("" + id);
	}

	@Override
	public void dispose() {
		socket.disconnect();
		playerShip.dispose();
		friendlyShip.dispose();

	}

	public void connectSocket() {
		try {
			socket = IO.socket(ip);
			socket.connect();

			id = socket.id();
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}

	private void configureSocketEvents() {

		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			public void call(Object... args) {
				Gdx.app.log("SocketIO", "Connected to server");
				
				socket.emit("imagame");
				
				player = new Starship(playerShip);
			}
		}).on("socketID", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];

				try {
					id = data.getString("id");
					Gdx.app.log("SocketIO", "ID has been set to: " + id);
				} catch (Exception e) {
					Gdx.app.log("SocketIO", "Error getting ID");
				}

			}
		}).on("newPlayer", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];

				try {
					tempId = data.getString("id");
					Gdx.app.log("SocketIO", "New player: " + tempId + " connected.");
					friendlyPlayers.put(tempId, new Starship(friendlyShip));
				} catch (Exception e) {
					Gdx.app.log("SocketIO", "Error getting New PlayerID");
				}

			}
		}).on("playerDisconnected", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];

				try {
					tempId = data.getString("id");
					friendlyPlayers.remove(tempId);
				} catch (Exception e) {
					Gdx.app.log("SocketIO", "Error getting disconnected player ID");
				}
			}
		}).on("getPlayers", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONArray objects = (JSONArray) args[0];
				Gdx.app.log("Debug", objects.toString());
				try {
					for (int i = 0; i < objects.length(); i++) {
						Gdx.app.log("System", id.toString() + ":" + objects.getJSONObject(i).getString("id").toString()
								+ objects.getJSONObject(i).getString("id").toString().equals(id.toString()));
						if (!objects.getJSONObject(i).getString("id").toString().equals(id.toString())) {

							Gdx.app.log("System", "Added player: " + objects.getJSONObject(i).getString("id"));

							Starship coopPlayer = new Starship(friendlyShip);
							Vector2 position = new Vector2();

							position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
							position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();

							coopPlayer.setPosition(position.x, position.y);

							friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer);
						}
					}
				} catch (Exception e) {

				}
			}
		}).on("playerUpdate", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				Gdx.app.log("System", "recieved update :" + data + "    self: " + id);
				try {
					if (friendlyPlayers.containsKey(data.getString("id"))) {
						friendlyPlayers.get(data.getString("id"))
								.setX(Double.valueOf(data.getString("x")).floatValue()); // .replaceAll("\\]|\\[",
																							// "")
						// friendlyPlayers.get(data.getString("id")).setY(Double.valueOf(data.getString("y").replaceAll("\\]|\\[",
						// "")).floatValue());
					}
				} catch (Exception e) {
					Gdx.app.log("Debug", "OOOPTH");
					e.printStackTrace(System.out);
				}
			}
		}).on(("fullUpdate"), new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONArray data = (JSONArray) args[0];
				try {
					// Gdx.app.log("fullUpdate", data.toString());
					JSONObject tempObj;

					for (int i = 0; i < data.length(); i++) {
						tempObj = data.getJSONObject(i);

						if (friendlyPlayers.containsKey(tempObj.getString("id"))) {
							friendlyPlayers.get(tempObj.getString("id"))
									.setX(Float.valueOf(String.valueOf(tempObj.getDouble("x"))));
						}
					}

				} catch (Exception e) {

				}
			}
		}).on(("kickplayer"), new Emitter.Listener() {
			
			@Override
			public void call(Object... args) {
				String userid = (String) args[0];
				if (userid.equals(id)) {
					socket.disconnect();
				}
			}
		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
			
			@Override
			public void call(Object... args) {
				game.setScreen(new MainMenu(game));
			}
		});
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

}
