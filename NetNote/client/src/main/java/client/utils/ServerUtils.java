/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.ConnectException;

import commons.*;
import javafx.collections.ObservableList;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class ServerUtils {

	private static final String SERVER = "http://localhost:8080/";

	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * This method check if you have selected note. If you have selected a note it will save it to the database
	 * @param currentPortNumber String the current port number of the current server
	 * @param selectedNote Note the currently selected note
	 */
	public void saveNoteToDatabase(String currentPortNumber, Note selectedNote) {
	if (selectedNote == null) {
		System.out.println("No note selected, skipping save operation.");
		return; // Prevent further execution
	}
	String dataBase = "http://localhost:"+currentPortNumber+"/notes";
	restTemplate.postForObject(dataBase, selectedNote, Void.class);
}


	/**
	 * Method that gets all the Notes affiliated with a given collection in the database
	 * @param currentPortNumber The port number to post on
	 * @param currentCollection The {@code Collection} to get the notes from.
	 * @return Returns a {@code Note[]} of notes which were found to be in the colletion.
	 */
    public Note[] getNotesFromCollectionDatabase(String currentPortNumber, Collection currentCollection) {
		String database = "http://localhost:"+currentPortNumber+"/collections" + "/" + currentCollection.getId() + "/notes";
		return restTemplate.getForObject(database, Note[].class);
	}

	/**
	 * Method that gets all collections from the database
	 * @param currentPortNumber The port of the server to get them from.
	 * @return Returns {@code Collection[]} of all the collections found
	 */
	public Collection[] getAllCollection(String currentPortNumber) {
		if (currentPortNumber != null){
			String dataBase = "http://localhost:"+currentPortNumber+"/collections";
			return restTemplate.getForObject(dataBase, Collection[].class);
		} else {
			String dataBase = "http://localhost:8080/collections";
			return restTemplate.getForObject(dataBase, Collection[].class);
		}
	}

	/**
	 * Method that gets all the Notes from the database
	 * @param currentPortNumber Port to get them from
	 * @return Returns {@code Note[]} of all the Notes that were returned.
	 */
	public Note[] getNotesDB(String currentPortNumber) {
		String dataBase = "http://localhost:"+currentPortNumber+"/notes";
		return restTemplate.getForObject(dataBase, Note[].class);
	}

	/**
	 * Method to get all the notes affiliated to a given collection.
	 * @see ServerUtils#getNotesFromCollectionDatabase(String, Collection)
	 * @param currentPortNumber Port number to get collection from
	 * @param currentCollection The collection to check
	 * @return Returns a {@code Note[]} of all the notes in the given collection on the given port.
	 */
	public Note[] getNotesCollection(String currentPortNumber, Collection currentCollection) {
		String dataBase = "http://localhost:"+currentPortNumber+"/collections/" + currentCollection.getId() + "/notes";
		if (currentCollection != null && currentCollection.getId() > 0){
			return restTemplate.getForObject(dataBase, Note[].class);
		}
		System.out.println("NAME: " + currentCollection.getName());
		return getNotesDB(currentPortNumber);
	}

	/**
	 * Method that checks if the given server is available.
	 * @return Returns {@code TRUE} when the server is available, and {@code FALSE} when it is not.
	 */
	public boolean isServerAvailable() {
		try {
			ClientBuilder.newClient(new ClientConfig()) //
					.target(SERVER) //
					.request(APPLICATION_JSON) //
					.get();
		} catch (ProcessingException e) {
			if (e.getCause() instanceof ConnectException) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the Embedded File by its ID
	 * @param id ID of the file
	 * @param currentCollection Port number
	 * @return Returns {@code NULL} when it couldn't find anything
	 */
	public EmbeddedFile getFileById(Long id, String currentCollection) {
		try {
			String url = "http://localhost:"+currentCollection +"/files/" + id;
			return restTemplate.getForObject(url, EmbeddedFile.class); // Fetch metadata
		} catch (Exception e) {
			System.err.println("Failed to fetch file with ID: " + id);
			return null;
		}
	}

	/**
	 * Method that moves files between notes
	 * @param fileId The ID of the file to move between notes.
	 * @param targetNoteId The ID of the note to move to.
	 * @param currentPort The PORT of the server to execute this change on.
	 */
	public void moveFileToAnotherNote(Long fileId, Long targetNoteId, String currentPort) {
		String url = String.format("http://localhost:%s/files/%d/move/%d", currentPort, fileId, targetNoteId);
		restTemplate.put(url, null);
	}

	/**
	 * Method that updates a note with new information on the database
	 * @param currentPortNumber The PORT of the server
	 * @param targetNote The NOTE to update
	 */
	public void updateNote(String currentPortNumber, Note targetNote) {
		String url = String.format("http://localhost:%s/notes/%d", currentPortNumber, targetNote.getId());
		restTemplate.put(url, targetNote);
	}

	/**
	 * Method to delete a given {@code Note} from the Database.
	 * @param id The Identification Number of the note.
	 * @param currentPortNumber The {@code Port Number} of the current Server.
	 * @param currentCollection The current {@code Collection} to delete the Note from.
	 * @param logger A {@code Logger} class for logging errors
	 */
	public void deleteNote(long id, String currentPortNumber, Collection currentCollection, Logger logger) {
		String dataBaseNoteDelete = "http://localhost:" + currentPortNumber + "/collections/" + currentCollection.getId() + "/notes/" + id;
		String database = "http://localhost:" + currentPortNumber + "/notes/" + id;
		try {
			restTemplate.delete(dataBaseNoteDelete);
			restTemplate.delete(database);
			System.out.println("Successfully deleted note with ID: " + id);
			String database2 = "http://localhost:" +currentPortNumber + "/undo/note/" + id;
			restTemplate.delete(database2, Void.class);
		} catch (Exception e) {
			logger.error("Failed to delete note");
		}
	}

	/**
	 * Method that gets the note connected to a given ID
	 * @param id The ID of the supposed note.
	 * @param currentPortNumber The PORT of the current server
	 * @return Returns the Note, or Null (depending on if it was found)
	 */
	public Note getNoteById(Long id, String currentPortNumber) {
		try {
			String url = "http://localhost:" + currentPortNumber + "/notes/" + id;
			return restTemplate.getForObject(url, Note.class);
		} catch (Exception e) {
			System.err.println("Failed to fetch note with ID: " + id);
			return null;
		}
	}

	/**
	 * Method that Updates a given note in the Database
	 * @param note The note to update
	 * @param currentPortNumber The PORT to post updates on
	 * @param logger The logger for logging errors
	 */
	public void updateNoteInDatabase(Note note, String currentPortNumber, Logger logger) {
		String dataBase = "http://localhost:" + currentPortNumber + "/notes";
		try {
			restTemplate.postForObject(dataBase, note, Void.class);
			System.out.println("Updated note with ID: " + note.getId());

		} catch (Exception e) {
			logger.error("Failed to update note with ID: {}", note.getId(), e);
		}
	}

	/**
	 * Method that handles moving a note from one to another collection
	 * @param currentPortNumber The PORT of the server
	 * @param currentCollection The {@code Collection} to move FROM.
	 * @param selectedNote The {@code Note} to move.
	 * @param selectedCollection The {@code Collection} to move TO.
	 */
	public void handleMovingNote(String currentPortNumber, Collection currentCollection,
								 Note selectedNote, Collection selectedCollection) {
		String dataBaseDel = "http://localhost:" + currentPortNumber + "/collections/" +
				currentCollection.getId() + "/notes/" +
				selectedNote.getId();
		String dataBasePost = "http://localhost:" + currentPortNumber + "/collections/" +
				selectedCollection.getId() + "/notes/" +
				selectedNote.getId();
		restTemplate.delete(dataBaseDel);
		restTemplate.postForObject(dataBasePost, selectedNote,
				Void.class);
	}

	/**
	 * Method that updates the position of a note (hierarchy)
	 * @param currentPortNumber The PORT of the server
	 * @param items An {@code ObservableList<Note>} of Notes which should be updated.
	 */
	public void updatePosition(String currentPortNumber, ObservableList<Note> items) {
		String dataBaseUrl = "http://localhost:" + currentPortNumber + "/notes";
		ObservableList<Note> notes =items;
		for (int i = 0; i < notes.size(); i++) {
			notes.get(i).setPosition(i);
			restTemplate.put(dataBaseUrl + "/" + notes.get(i).getId(), notes.get(i));
		}
	}

	/**
	 * Posts the Default collection to the database.
	 * @param defaultCollection Default {@code Collection }
	 * @param currentPortNumber PORT number of server
	 */
	public void createDefault(Collection defaultCollection, String currentPortNumber) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Collection> requestEntity = new HttpEntity<>(defaultCollection, headers);

		String database = "http://localhost:" + currentPortNumber + "/collections";
		restTemplate.postForObject(database, requestEntity, Void.class);
	}

	/**
	 * Method that initiates editing anything that can be processed using UNDO items.
	 * @param undoAction The action that was edited
	 * @param currentPortNumber The PORT number
	 */
	public void startEdit(UndoItem undoAction, String currentPortNumber) {
		String database2 = "http://localhost:"+currentPortNumber +"/undo";
		restTemplate.postForObject(database2, undoAction, Void.class);
	}

	/**
	 * Resolving Method for Deleting a note from one database and adding it to another
	 * @param currentPortNumber The PORT of the server
	 * @param selected The Note which should be changed
	 * @param collectionId The ID of the collection
	 */
	public void resolveMove(String currentPortNumber, Note selected, long collectionId) {
		String dataBase3 = "http://localhost:"+ currentPortNumber +"/collections/Note/" + selected.getId();
		String dataBaseDel = "http://localhost:" + currentPortNumber + "/collections/" +
				restTemplate.getForObject(dataBase3, Long.class) + "/notes/" +
				selected.getId();
		String dataBasePost = "http://localhost:" + currentPortNumber+ "/collections/" +
				collectionId + "/notes/" +
				selected.getId();
		restTemplate.delete(dataBaseDel);
		restTemplate.postForObject(dataBasePost, selected,
				Void.class);
	}

	/**
	 * Method that gets all undoActions from the database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @return Returns {@code UndoItem[]}: a list of undoActions
	 */
	public UndoItem[] getUndoActions(String currentPortNumber) {
		String dataBase = "http://localhost:"+currentPortNumber+"/undo";
		return restTemplate.getForObject(dataBase, UndoItem[].class);
	}

	/**
	 *  Method that deletes files from database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void deleteFile(String currentPortNumber) {
		String database2 = "http://localhost:" + currentPortNumber + "/undo";
		restTemplate.delete(database2, Void.class);
		restTemplate.delete(database2, Void.class);
	}

	/**
	 * Method that deletes undoItems from database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void deleteUndoItem(String currentPortNumber) {
		String database2 = "http://localhost:"+currentPortNumber+"/undo";
		restTemplate.delete(database2, Void.class);
	}

	/**
	 * Method that deletes UndoItem by its specific ID
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id ID of undoitem
	 */
	public void deleteUndoItembyId(String currentPortNumber, long id) {
		String database2 = "http://localhost:"+currentPortNumber+"/undo/" + id;
		restTemplate.delete(database2, Void.class);
	}

	/**
	 * Method that renames files in Database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id  Id of the File to update
	 * @param oldName Old Name of the File to Update
	 * @return Returns {@code RestTemplate.exchange()} Exceptions if applicable.
	 */
	public ResponseEntity<EmbeddedFile> renameFile(String currentPortNumber, Long id, String oldName) {
		String url = "http://localhost:"+currentPortNumber+"/files/" + id + "/rename";
		HttpEntity<String> request = new HttpEntity<>(oldName);
		return restTemplate.exchange(url, HttpMethod.PUT, request, EmbeddedFile.class);
	}

	/**
	 * Deletes all Undo Items from database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void deleteUndoAll(String currentPortNumber) {
		String database2 = "http://localhost:"+currentPortNumber+"/undo/all";
		restTemplate.delete(database2, Void.class);
	}

	/**
	 * Updates an Undo Item in database
	 * @param undoAction The undoAction to change
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void updateUndoItem(UndoItem undoAction, String currentPortNumber) {
		String database2 = "http://localhost:"+currentPortNumber+"/undo";
		restTemplate.postForObject(database2, undoAction, Void.class);

	}

	/**
	 * Method that updates the Note in Collection
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id The id of the Note to update
	 * @param newNote The new note to update with
	 */
	public void updateNoteInCollection(String currentPortNumber, Long id, Note newNote) {
		String dataBase = "http://localhost:"+currentPortNumber+"/collections/" + id + "/notes/" + newNote.getId();
		restTemplate.postForObject(dataBase, newNote, Void.class);
	}

	/**
	 * Updated UNDO method for performing database tasks
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void undoUpdate(String currentPortNumber) {
		String database2 = "http://localhost:"+currentPortNumber+"/undo/update";
		restTemplate.delete(database2, Void.class);
	}

	/**
	 * More Undo
	 * @param undoMore UndoITem
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void undoMore(UndoItem undoMore, String currentPortNumber) {
		String database2 = "http://localhost:"+currentPortNumber+"/undo";
		restTemplate.delete(database2, Void.class);
		restTemplate.postForObject(database2, undoMore, Void.class);
	}

	/**
	 * Method that gets the Collection with the given ID
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id The ID of the collection
	 * @return Returns a possible exception
	 */
	public Collection getNoteFromColelction(String currentPortNumber, String id) {
		String dataBase = "http://localhost:"+currentPortNumber+"/collections/" + id;
		return restTemplate.getForObject(dataBase, Collection.class);
	}

	/**
	 * Second function for updating UndoItems in database
	 * @param undoAction UndoAction to update
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void updateUndoItem2(UndoItem undoAction, String currentPortNumber) {
		String database2 = "http://localhost:"+currentPortNumber+"/undo";
		restTemplate.postForEntity(database2, undoAction, Void.class);
	}

	/**
	 * Method that gets the response from File in Database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id The ID of the File
	 * @param body The HTTPEntity Body
	 * @param headers The HTTPENtity Headers
	 * @return Returns a possible Exception caused by REST
	 */
	public ResponseEntity<EmbeddedFile> getResponse(
			String currentPortNumber, long id, MultiValueMap<String, Object> body, HttpHeaders headers) {
		String url = "http://localhost:"+currentPortNumber+"/files/" + id;
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		return restTemplate.exchange(url, HttpMethod.POST,
				requestEntity, EmbeddedFile.class);
	}

	/**
	 * Method to post Undo Item on database
	 * @param undoAction The UndoAction to post
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void postUndoItem(UndoItem undoAction, String currentPortNumber) {
		String dataBase = "http://localhost:"+currentPortNumber+"/undo";
		restTemplate.postForObject(dataBase, undoAction, Void.class);
	}

	/**
	 * Method to get a Response out of Http.DELETE
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id The id of the File
	 * @return Returns a possible Exception caused by REST
	 */
	public ResponseEntity<Void> getResponseDelete(String currentPortNumber, Long id) {
		String url = "http://localhost:"+currentPortNumber+"/files/" + id;
		return restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);

	}

	/**
	 * Method to get a response out of Putting a file by renaming
	 * @param request The Request HTTP entity
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id The ID of the file
	 * @return Returns a possuble Exception caused by REST
	 */
	public ResponseEntity<EmbeddedFile> getResponsePut(
			HttpEntity<String> request, String currentPortNumber, Long id){
		String url = "http://localhost:"+currentPortNumber+"/files/" + id + "/rename";
		return restTemplate.exchange(url, HttpMethod.PUT, request, EmbeddedFile.class);
	}

	/**
	 *  Method to post a collection on database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param selectedCollection Collection to post
	 */
	public void postCollections(String currentPortNumber, Collection selectedCollection) {
		String database = "http://localhost:" + currentPortNumber+"/collections";
		restTemplate.postForObject(database, selectedCollection, Void.class);
	}

	/**
	 * Post a collection on database using RequestEntity
	 * @param requestEntity The HTTP entity of the post
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 */
	public void postCollectionsEntity(HttpEntity<Collection> requestEntity, String currentPortNumber) {
		// try posting to check for errors
		String dataBase = "http://localhost:"+currentPortNumber+"/collections";
		restTemplate.postForObject(dataBase,requestEntity, Void.class);
	}

	/**
	 * Deletes a collection from the database
	 * @param currentPortNumber The {@code Port Number} of the current {@code Server}.
	 * @param id The ID of the collection to Delete
	 */
	public void deleteCollection(String currentPortNumber, Long id) {
		String dataBase = "http://localhost:"+currentPortNumber+"/collections/" + id;
		restTemplate.delete(dataBase);
	}

}