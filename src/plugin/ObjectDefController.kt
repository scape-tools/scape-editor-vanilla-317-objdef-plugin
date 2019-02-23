package plugin

import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import scape.editor.fs.RSArchive
import scape.editor.fx.TupleCellFactory
import scape.editor.gui.App
import scape.editor.gui.controller.BaseController
import scape.editor.gui.event.LoadCacheEvent
import scape.editor.gui.event.LoadObjectDefEvent
import scape.editor.gui.event.SaveObjectDefEvent
import scape.editor.gui.model.KeyModel
import scape.editor.gui.model.NamedValueModel
import scape.editor.gui.model.ValueModel
import scape.editor.gui.plugin.PluginManager
import java.net.URL
import java.util.*

class ObjectDefController : BaseController() {

    @FXML
    lateinit var indexTable: TableView<KeyModel>

    @FXML
    lateinit var indexIdCol : TableColumn<KeyModel, Int>

    @FXML
    lateinit var indexNameCol : TableColumn<KeyModel, String>

    @FXML
    lateinit var dataTable : TableView<NamedValueModel>

    @FXML
    lateinit var dataNameCol : TableColumn<NamedValueModel, String>

    @FXML
    lateinit var dataValueCol : TableColumn<NamedValueModel, ValueModel>

    val indexes = FXCollections.observableArrayList<KeyModel>()

    val data = FXCollections.observableArrayList<NamedValueModel>()

    @FXML
    lateinit var keyTf: TextField

    @FXML
    lateinit var valueTf: TextField

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        indexIdCol.setCellValueFactory { it.value.idProperty.asObject() }
        indexNameCol.setCellValueFactory { it.value.nameProperty }

        indexTable.selectionModel.selectedItemProperty().addListener { _, _, newValue ->

            if (newValue == null || newValue.id < 0) {
                return@addListener
            }

            data.clear()

            for (set in newValue.map) {
                data.add(NamedValueModel(set.key, ValueModel(newValue, set.key, set.value)))
            }

        }

        val filteredKeyList = FilteredList(indexes) { true }
        keyTf.textProperty().addListener { _, _, newValue -> filteredKeyList.setPredicate {
            if (newValue == null || newValue.isEmpty()) {
                return@setPredicate true
            }

            val lowercase = newValue.toLowerCase()

            if (it.name.toLowerCase().contains(lowercase) || it.id.toString() == lowercase) {
                return@setPredicate true
            }

            return@setPredicate false
        }
        }

        val sortedKeyList = SortedList(filteredKeyList)
        sortedKeyList.comparatorProperty().bind(indexTable.comparatorProperty())
        indexTable.items = sortedKeyList

        dataNameCol.setCellValueFactory { it.value.nameProperty }
        dataValueCol.setCellValueFactory { it.value.valueProperty }
        dataValueCol.setCellFactory { TupleCellFactory() }

        val filteredValueList = FilteredList(data) { true}
        valueTf.textProperty().addListener { _, _, newValue -> filteredValueList.setPredicate {
            if (newValue == null || newValue.isEmpty()) {
                return@setPredicate true
            }

            val lowercase = newValue.toLowerCase()

            if (it.name.toLowerCase().contains(lowercase)) {
                return@setPredicate true
            }

            return@setPredicate false
        }
        }

        val sortedValueList = SortedList(filteredValueList)
        sortedValueList.comparatorProperty().bind(dataTable.comparatorProperty())

        dataTable.items = sortedValueList
    }

    override fun onPopulate() {
        data.clear()
        indexes.clear()
        PluginManager.post(LoadCacheEvent(App.fs))

        val archive = App.fs.getArchive(RSArchive.CONFIG_ARCHIVE)
        PluginManager.post(LoadObjectDefEvent(currentPlugin, archive, indexes))
    }

    override fun onClear() {
        data.clear()
        indexes.clear()
    }

    @FXML
    private fun goBack() {
        switchScene("StoreScene")
    }

    @FXML
    fun onSave() {
        val archive = App.fs.getArchive(RSArchive.CONFIG_ARCHIVE)
        PluginManager.post(SaveObjectDefEvent(currentPlugin, archive, indexes))
    }

}