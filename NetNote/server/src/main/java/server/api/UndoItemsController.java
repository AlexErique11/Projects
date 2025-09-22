package server.api;


import commons.UndoItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.database.UndoItemRepository;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/undo")
public class UndoItemsController {
    private UndoItemRepository ud;

    /**
     * Constructor
     * @param undoRepository - Undo Items Repository
     */
    public UndoItemsController(UndoItemRepository undoRepository) {
        this.ud = undoRepository;
    }

    /**
     * This method is used to get all UndoItems from database
     * @return a list of undoItems
     */
    @GetMapping
    @ResponseBody
    public List<UndoItem> getAllUndoItems(){
        return ud.findAll();
    }


    /**
     * This method is used to return a list of all collection
     * IDs present in any UndoItems
     * @return a list of Long
     */
    @GetMapping(value = "/collections")
    @ResponseBody
    public List<Long> getCollectionIDs(){
        return ud.findAll().stream()
                .map(UndoItem::getCollectionId)
                .filter(undoItem -> undoItem != -1).toList();
    }

    /**
     * This method is used to return a list of all files
     * IDs present in any Undo Items
     * @return a list of Long
     */
    @GetMapping(value = "/files")
    @ResponseBody
    public List<Long> getFileIDs(){
        return ud.findAll().stream()
                .map(UndoItem::getFileId)
                .filter(undoItem -> undoItem != -1).toList();
    }


    /**
     * This method is used to get an Undo Item
     * @param id id of undo item
     * @return a single undo item
     */
    @GetMapping("/{id}")
    @ResponseBody
    public UndoItem getUndoItem(@PathVariable Long id) {
        return ud.findById(id).orElse(null);
    }


    /**
     * This method post a new UndoItem to the database
     * @param undo an undo item to post
     * @return undoItem
     */
    @PostMapping
    public ResponseEntity<UndoItem> postUndoItem(@RequestBody UndoItem undo) {
        return new ResponseEntity<>(ud.save(undo), HttpStatus.CREATED);
    }


    /**
     * This mapping is used to delete an undo item with
     * a given ID
     * @param id of the note to be deleted
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public void deleteUndoItem(@PathVariable Long id){
        UndoItem c = ud.findById(id).get();
        ud.delete(c);
    }

    /**
     * This mapping is used to delete the most recent undo Item
     */
    @DeleteMapping
    @ResponseBody
    public void delete(){
        List<UndoItem> undoItem = ud.findAll();
        long z = 0;
        for(int i = 0; i < undoItem.size(); i++){
            if(undoItem.get(i).getId() > z){
                z = i;
            }
        }
        UndoItem highestIdItem = undoItem.get((int) z);
        ud.delete(highestIdItem);
    }

    /**
     * This mapping is used to delete all Undo Items
     */
    @DeleteMapping(value = "/all")
    @ResponseBody
    public void deleteAll(){
        ud.deleteAll();
    }

    /**
     * This mapping is used to delete the oldest Undo Items if they exceed the number of 30
     */
    @DeleteMapping(value = "/update")
    @ResponseBody
    public void deleteNoteUndoActions(){
        List<UndoItem> undoItem = ud.findAll();
        if(undoItem.size() > 30){
            int z = undoItem.size() - 40;
            for(int i = 0; i < z; i++) {
                undoItem = ud.findAll();
                UndoItem lowestIdItem = undoItem.stream()
                        .min(Comparator.comparing(UndoItem::getId)).get();
                ud.delete(lowestIdItem);
            }
        }

    }

    /**
     * This mapping is used to delete all Undo Items connected to a Note
     * @param id of the note
     */
    @DeleteMapping(value = "/note/{id}")
    @ResponseBody
    public void deleteNoteUndoActions(@PathVariable Long id){
        List<UndoItem> undoItem = ud.findAll();
        for (UndoItem item : undoItem) {
            if (item.getNote() == id) {
                ud.delete(item);
            }
        }
    }

}
