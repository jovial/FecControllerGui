/**
  * Created by fluxoid on 17/02/17.
  */

import java.util
import java.util.concurrent.atomic.AtomicInteger
import java.util.prefs.Preferences
import javafx.event
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import org.cowboycoders.ant
import org.cowboycoders.ant.interfaces.AntTransceiver
import org.cowboycoders.ant.profiles.FecProfile
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger
import org.cowboycoders.ant.profiles.common.events._
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent
import org.cowboycoders.ant.profiles.fitnessequipment.Defines.TrainerStatusFlag
import org.cowboycoders.ant.profiles.fitnessequipment.pages._
import org.cowboycoders.ant.profiles.fitnessequipment.{Capabilities, Config, ConfigBuilder, Defines}

import scala.language.implicitConversions
import scala.util.Try
import scalafx.Includes.{when, _}
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.binding.{Bindings, ObjectBinding}
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty, StringProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node, Parent, Scene}



private case class LabelValuePair(label: String, initialValue : String = "unknown") {
  val value = new StringProperty() {
    value = initialValue
  }
}

case class Cell(label: String, value: StringProperty) {
  def this (pair: LabelValuePair) {
    this(pair.label, pair.value)
  }
}

case class GridCellGroup(enabled: Option[BooleanProperty], cells: Seq[Cell])

object FecControllerMain extends JFXApp {
  val unknownStr: String = "unknown"

  private val SHOW_SIMULATION_PREF = "showSimulation"
  private val SHOW_COMMON_PREF = "showCommon"
  private val SHOW_CAPS_PREF = "showCaps"
  private val SHOW_CONF_PREF = "showConf"
  private val SHOW_STATE_PREF = "showState"

  val pref: Preferences = Preferences.userNodeForPackage(FecControllerMain.getClass)

  val antInterface: AntTransceiver = new AntTransceiver(1)
  val antNode: ant.Node = new org.cowboycoders.ant.Node(antInterface)


  val power = new StringProperty() {
    value = unknownStr
  }

  val speed = new StringProperty() {
    value = unknownStr
  }

  val hr = new StringProperty() {
    value = unknownStr
  }

  val cadence = new StringProperty() {
    value = unknownStr
  }

  val distance = new StringProperty() {
    value = unknownStr
  }

  val laps = new StringProperty() {
    value = "0"
  }

  val gradient = new StringProperty
  val coeffRolling = new StringProperty

  val bikeWeight = new StringProperty
  val userWeight = new StringProperty
  val userAge = new StringProperty
  val userHeight = new StringProperty
  val wheelDiameter = new StringProperty
  val windSpeed = new StringProperty
  val windCoeff = new StringProperty
  val draftingFactor = new StringProperty

  val powerLabel = "Power/w"
  val speedLabel = "Speed/kmh"
  val heartRateLabel = "Heart rate/bpm"
  val cadenceLabel = "Cadence/rpm"
  val distanceLabel = "Distance/km"
  val gearRatioLabel = "Gear ratio"
  val lapsLabel = "Laps"
  val bikeWeightLabel = "Bike weight/kg"
  val userWeightLabel = "User weight/kg"
  val userAgeLabel = "User age/years"
  val userHeightLabel = "User height/cm"
  val wheelDiaLabel = "Wheel diameter/m"
  val coeffRollingLabel = "Coeff. rolling r."
  val gradientLabel = "Gradient/%"

  val windSpeedLabel = "Wind speed/m/s"
  val windCoeffLabel = "Wind coeff."
  val draftingFactorLabel = "Drafting factor"

  // capabilites
  val supportBasicLabel = "Basic resistance support"
  val supportBasicState = new StringProperty(unknownStr)
  val supportSimulationLabel = "Simulation support"
  val supportSimulationState = new StringProperty(unknownStr)
  val supportPowerLabel = "Const. power support"
  val supportPowerState = new StringProperty(unknownStr)
  val maxResistanceLabel = "Max. resistance"
  val maxResistanceState = new StringProperty(unknownStr)

  // config
  val bicycleWheelDiameterLabel = "Wheel diameter"
  val userWeightState = new StringProperty(unknownStr)
  val bikeWeightState = new StringProperty(unknownStr)
  val gearRatioState = new StringProperty(unknownStr)
  val bicycleWheelDiameterState = new StringProperty(unknownStr)

  // power from summation + event count
  val powerAvg = new StringProperty() {
    value = unknownStr
  }
  val powerAvgLabel = "Avg Power/w"

  def boolToStr(b: java.lang.Boolean): String = {
    val nonNull = Option(b).getOrElse(false)
    // TODO: internationalization
    nonNull.toString
  }


  private val equipmentState: LabelValuePair = LabelValuePair("Equipment state")
  private val calibrationSpeedState: LabelValuePair = LabelValuePair("Speed condition")
  private val calibrationTargetSpeed: LabelValuePair = LabelValuePair("Target Speed")
  private val calibrationTargetSpin: LabelValuePair = LabelValuePair("Target Spindown time")
  private val tempCondition: LabelValuePair = LabelValuePair("Temperature status")
  private val calibrationSpinDown: LabelValuePair = LabelValuePair("Spindown time")
  private val calibrationZeroOffset: LabelValuePair = LabelValuePair("Zero offset")
  private val temperature: LabelValuePair = LabelValuePair("Temperature")
  private val calibrationSuccess: LabelValuePair = LabelValuePair("Calibration success")
  private val coastingPair: LabelValuePair = LabelValuePair("Coasting")

  private val connected = new BooleanProperty {
    value = false
  }


  private def updateCapabilites(caps: Capabilities): Unit = {
    supportBasicState.value = boolToStr(caps.isBasicResistanceModeSupported)
    supportPowerState.value = boolToStr(caps.isTargetPowerModeSupported)
    supportSimulationState.value = boolToStr(caps.isSimulationModeSupported)
    maxResistanceState.value = Option(caps.getMaximumResistance).map(_.toString).getOrElse(unknownStr)
  }

  private def updateConfig(conf: Config): Unit = {
    userWeightState.value = "%2.2f".format(conf.getUserWeight.floatValue())
    bikeWeightState.value = "%2.2f".format(conf.getBicycleWeight.floatValue())
    gearRatioState.value = "%2.2f".format(conf.getGearRatio.floatValue())
    bicycleWheelDiameterState.value = "%2.2f".format(conf.getBicycleWheelDiameter.floatValue())
  }

  var allToggles: List[DisplayToggle] = List[DisplayToggle]()

  case class DisplayToggle(key: String, desc: String) {
    val toggle: BooleanProperty = mkPersistentBool(key)
    allToggles = this :: allToggles
  }

  private def mkPersistentBool(prefId: String): BooleanProperty = {
    val ret = new BooleanProperty {
      value = pref.getBoolean(prefId, false)
    }
    ret.onChange{ (_, _, n) => {
      pref.putBoolean(prefId, n)}}
    ret
  }


  // order matters
  val stateToggle = DisplayToggle(SHOW_STATE_PREF, "State")
  val configToggle = DisplayToggle(SHOW_CONF_PREF, "Config")
  val capabilitiesToggle = DisplayToggle(SHOW_CAPS_PREF, "Capabilities")
  val simulationToggle = DisplayToggle(SHOW_SIMULATION_PREF, "Simulation data")
  val telemetryToggle = DisplayToggle(SHOW_COMMON_PREF, "Common Data")

  val testGrid = new GridPane() {
    hgap = 10
    vgap = 10
  }

  val telemetryCells = Array(
    GridCellGroup(None, Array(Cell(powerLabel, power), Cell(powerAvgLabel, powerAvg),
      Cell(speedLabel, speed), Cell(heartRateLabel, hr),
      Cell(cadenceLabel, cadence), Cell(distanceLabel, distance), Cell(lapsLabel, laps)))
  )

  val simulationCells = Array(GridCellGroup(Some(simulationToggle.toggle), Array(
    Cell(bikeWeightLabel, bikeWeight), Cell(userWeightLabel, userWeight), Cell(userAgeLabel, userAge),
    Cell(userHeightLabel, userHeight), Cell(wheelDiaLabel, wheelDiameter), Cell(coeffRollingLabel, coeffRolling),
    Cell(gradientLabel, gradient), Cell(windSpeedLabel, windSpeed), Cell(windCoeffLabel, windCoeff),
    Cell(draftingFactorLabel, draftingFactor))))

  val capabilitiesCells = Array(GridCellGroup(None,
    Array(
      Cell(supportBasicLabel, supportBasicState),
      Cell(maxResistanceLabel, maxResistanceState),
      Cell(supportSimulationLabel, supportSimulationState),
      Cell(supportPowerLabel, supportPowerState)
    )))

  val configCells = Array(GridCellGroup(None, Array(
    Cell(bikeWeightLabel, bikeWeightState),
    Cell(bicycleWheelDiameterLabel, bicycleWheelDiameterState),
    Cell(userWeightLabel, userWeightState),
    Cell(gearRatioLabel, gearRatioState)
  )))

  val stateCells = Array(GridCellGroup(None, Array (
    new Cell(equipmentState),
    new Cell(calibrationSpinDown),
    new Cell(calibrationZeroOffset),
    new Cell(calibrationSuccess),
    new Cell(calibrationSpeedState),
    new Cell(calibrationTargetSpeed),
    new Cell(calibrationTargetSpin),
    new Cell(temperature),
    new Cell(tempCondition),
    new Cell(coastingPair)
  )))

  def boolToStr(b: BooleanProperty): StringProperty = {
    val res = new StringProperty()
    res <== when (b) choose boolToStr(true) otherwise boolToStr(false)
    res
  }

  val trainerStatusProp = new ObjectProperty[util.EnumSet[TrainerStatusFlag]] {
    value = util.EnumSet.noneOf(classOf[TrainerStatusFlag])
  }

  val trainerFlagToState: Map[TrainerStatusFlag, BooleanProperty] = TrainerStatusFlag.values()
    .zip(TrainerStatusFlag.values().map(_ => new BooleanProperty() { value = false })).toMap

  trainerStatusProp.onChange {
    (_,_, n) => {
      for (flag <- TrainerStatusFlag.values()) {
        trainerFlagToState(flag).value = n.contains(flag)
      }
    }
  }

  val trainerStatusCells = Array(
    GridCellGroup(None, TrainerStatusFlag.values().map(flag => Cell(flag.toString(), boolToStr(trainerFlagToState(flag)))))
  )

  val numCols = new IntegerProperty() {
    value = 1
  }

  val statusMessage = new StringProperty()

  val statusCount = new AtomicInteger(0)

  private def setStatusMsg(a: String): Unit = {
    val id = statusCount.addAndGet(1)
    statusMessage.value = id + ": " + a
  }

  setStatusMsg("No message")

  def decorateWithStatusMsg(button: Button, onAction: EventHandler[event.ActionEvent])
    : javafx.event.EventHandler[javafx.event.ActionEvent] = (ae : ActionEvent) => {
      setStatusMsg(button.text.value)
      onAction.handle(ae)
    }


  def genSplit: Parent = {


    val statsVBox = new VBox()

    statsVBox.vgrow = Priority.Always

    val scroll = new ScrollPane() {
      content = statsVBox
      fitToWidth = true
      fitToHeight = false
    }

    scroll.layoutBounds.onChange {
      (_, _, newV) => {
        val size = newV.width
        var cols = 1
        if (size >= 600) {
          cols = 5
        } else if (size >= 380) {
          cols = 3
        } else if (size >= 200) {
          cols = 2
        }

        numCols.value = cols

      }
    }

    def genGridTitle(title: String): Label = {
      val label = new Label(title) {
        style = "-fx-font-size:20"
      }
      label
    }

    statsVBox.children += genDecorated(telemetryCells, scroll.viewportBounds, telemetryToggle.toggle, genGridTitle("Common telemetry"))
    statsVBox.children += genDecorated(simulationCells, scroll.viewportBounds, simulationToggle.toggle, genGridTitle("Simulation data"))
    statsVBox.children += genDecorated(capabilitiesCells, scroll.viewportBounds, capabilitiesToggle.toggle, genGridTitle("Capabilities"))
    statsVBox.children += genDecorated(configCells, scroll.viewportBounds, configToggle.toggle, genGridTitle("Config"))
    statsVBox.children += genDecorated(stateCells, scroll.viewportBounds, stateToggle.toggle, genGridTitle("State"))
    statsVBox.children += genDecorated(trainerStatusCells, scroll.viewportBounds, stateToggle.toggle, genGridTitle("Trainer status"))

    val powerInput = labelNode(StringProperty("Power"))(
      mkInput(
        StringProperty("Set power (w)"),
        validateDecimal,
        getInvalidNumTxt,
        (v) => turbo.setTargetPower(v.toDouble)
      )
    )

    val hrInput = labelNode(StringProperty("Basic resistance"))(
      mkInput(
        StringProperty("Set basic resistance (%/units)"),
        validateDecimal,
        getInvalidNumTxt,
        (v) => turbo.setBasicResistance(v.toDouble)
      )
    )



    def mkSendConf: Node = {


      val rearRing = mkInput(
        StringProperty(" value (teeth)"),
        validateNumber,
        getInvalidNumTxt,
        (_) => {},
        (_) => {}
      )

      val frontRing = mkInput(
        StringProperty(" value (teeth)"),
        validateNumber,
        getInvalidNumTxt,
        (_) => {rearRing.requestFocus()},
        (_) => {}
      )

      val dia = mkInput(
        StringProperty("value (m)"),
        validateDecimal,
        getInvalidNumTxt,
        (_) => {frontRing.requestFocus()},
        (_) => {}
      )

      val bWeight = mkInput(
        StringProperty(" value (kg)"),
        validateDecimal,
        getInvalidNumTxt,
        (_) => {dia.requestFocus()},
        (_) => {}
      )

      val uWeight = mkInput(
        StringProperty(" value (kg)"),
        validateDecimal,
        getInvalidNumTxt,
        (_) => {bWeight.requestFocus()},
        (_) => {}
      )


      val sendConf = new Button {
        text = "send config"
        onAction = { (_: ActionEvent) => {
          if (bWeight.text.value == "" || uWeight.text.value == "" || dia.text.value == "" || frontRing.text.value == "" || rearRing.text.value == "" ) {
            new Alert(AlertType.Error) {
              title = "Error"
              contentText = "you must fill in all fields with valid data"
            }.showAndWait()
          } else {
            val ratio = frontRing.text.value.toDouble / rearRing.text.value.toDouble
            val c = new ConfigBuilder()
              .setBicycleWeight(new java.math.BigDecimal(bWeight.text.value))
              .setBicycleWheelDiameter(new java.math.BigDecimal(dia.text.value))
              .setUserWeight(new java.math.BigDecimal(uWeight.text.value))
              .setGearRatio(new java.math.BigDecimal(ratio))
              .createConfig()
            turbo.setConfig(c)
            setStatusMsg("new config sent")
          }
        } }
        hgrow = Priority.Always
        maxWidth = Double.MaxValue
      }

      new VBox() {
        children = Seq(labelNode(StringProperty("User weight"))(uWeight),
          labelNode(StringProperty("Bike weight"))(bWeight),
          labelNode(StringProperty("Wheel diameter"))(dia),
          labelNode(StringProperty("front ring"))(frontRing),
          labelNode(StringProperty("rear ring"))(rearRing),
          sendConf)
      }
    }

    def genToggle(displayToggle: DisplayToggle): CheckBox = {
      val box = new CheckBox {
        text = displayToggle.desc
        selected = displayToggle.toggle.value
      }
      displayToggle.toggle <==> box.selected
      box
    }

    val checkBoxContainer = new VBox() {
      children = new Label("data to display:") :: allToggles.map(genToggle)
    }


    val reqCapsButton = new Button {
      text = "Request capabilities"
      onAction = { (_: ActionEvent) => turbo.requestCapabilities() }
      hgrow = Priority.Always
      maxWidth = Double.MaxValue
    }

    val reqCalibrationButton = new Button {
      text = "Request calibration (spindown)"
      onAction = { (_: ActionEvent) => turbo.requestSpinDownCalibration() }
      hgrow = Priority.Always
      maxWidth = Double.MaxValue
    }

    val reqZeroOffsetCalibrationButton = new Button {
      text = "Request calibration (zero offset)"
      onAction = { (_: ActionEvent) => turbo.requestZeroOffsetCalibration()}
      hgrow = Priority.Always
      maxWidth = Double.MaxValue
    }

    val reqConfigButton = new Button {
      text = "Request Config"
      onAction =  (_: ActionEvent) => turbo.requestConfig()
      hgrow = Priority.Always
      maxWidth = Double.MaxValue
    }

    val requestButtons = Seq(reqCapsButton,reqConfigButton, reqCalibrationButton, reqZeroOffsetCalibrationButton)

    for (button <- requestButtons) {
      button.onAction = decorateWithStatusMsg(button, button.onAction.getValue)
    }

    //groups buttons
    val buttonVBox = new VBox() {
      children = requestButtons
    }


    val rightVBox = new VBox() {
      children = Seq(powerInput, hrInput, buttonVBox, checkBoxContainer, mkSendConf)
      spacing = 10
    }


    val rightSide = new ScrollPane {
      content = rightVBox
      fitToWidth = true
    }

    val border = new BorderPane


    val statusLight = new Rectangle {
      height = 30
      fill <== when(connected) choose Color.Green otherwise Color.Red
      width = 30
    }


    val statusText = new Text {
      text <== statusMessage
    }



    val statusPane = new BorderPane {
      center = statusText
      hgrow = Priority.Always
      style = "-fx-background-color: white"
    }

    val status = new HBox {
      children = Seq(statusLight, statusPane)
      style = "-fx-border-width: 1; -fx-border-style: solid;"
    }

    border.center = scroll
    border.right = rightSide
    border.bottom = status

    border
  }

  private def clearText(wrapped: TextField): Unit = {
    wrapped.text.value = ""
  }

  def mkInput(promptTxt: StringProperty, validate: (scalafx.scene.control.TextField) => Boolean,
              getErrorTxt: (scalafx.scene.control.TextField) => String, onAccept: (String) => Unit,
              onAfterAccept: (TextField) => Unit = clearText): TextField = {
    val inputField = new TextField() {
      promptText <== promptTxt
    }

    // validate an integer
    inputField.focused.onChange {
      (
        _, _, n) =>
        if (!n && !validate(inputField))
        // unfocused
          inputField.text = ""
    }

    inputField.onAction = (_: ActionEvent) => {
      if (validate(inputField)) {
        onAccept(inputField.text.value)
        onAfterAccept(inputField)
      } else new Alert(AlertType.Error) {
        title = "Error"
        contentText = getErrorTxt(inputField)
      }.showAndWait()

    }

    inputField
  }

  private def labelNode(labelTxt: StringProperty)(a:Node): Node = {
    val inputLabel = new Label {
      text <== labelTxt
      labelFor = a
    }

    val inputPair = new VBox {
      //margin = Insets(10)
      padding = Insets(5)
      children = Seq(inputLabel, a)
    }
    inputPair
  }

  private def getInvalidNumTxt(field: scalafx.scene.control.TextField)= {
    field.text.value + " is invalid. You must enter a valid number"
  }

  private def validateNumber(field: scalafx.scene.control.TextField) = field.text.value.matches("[0-9]+")
  private def validateDecimal(field: scalafx.scene.control.TextField) = field.text.value.matches("[0-9]+([.][0-9]+)?")


  // need object bindings to live as long as Node they are affecting
  private val bindingKeepAlive = scala.collection.mutable.HashMap[Node, ObjectBinding[_]]()

  /**
    * Ensure this object lives as long as you want the scaling to occur. The binding doesn't keep this object alive.
    * garbage collected)
    */
  def genDecorated(cells: Seq[GridCellGroup], parentBounds: ObjectProperty[javafx.geometry.Bounds], enabled: BooleanProperty, header: Node): Group = {

    //TODO: scale header separately?
    val commonTelemetry = new DynamicTable(cells, parentBounds, numCols)

    val decoratedCommonTele = new VBox {

      children = Seq(new Group {
        children = header
      }, new Group {
        children = commonTelemetry
      })
      alignment = Pos.Center
    }

    decoratedCommonTele.visible <== enabled


    val scaleBinding = Bindings.createObjectBinding(() => {
      val x = decoratedCommonTele.layoutBounds.get.getWidth
      val y = parentBounds.value.getWidth
      if (x == 0.0 || y == 0.0) 1 else {
        val ret = 0.99 * y / x
        ret
      }

    }, decoratedCommonTele.layoutBounds, parentBounds)

    scaleBinding.onChange {
      (_, _, n) => {
        decoratedCommonTele.scaleY = n
        decoratedCommonTele.scaleX = n
      }
    }

    val ret = new Group {
      children = decoratedCommonTele
    }

    //make sure object binding lives long enough
    bindingKeepAlive(ret) = scaleBinding

    ret
  }


  class DynamicTable(cells: Seq[GridCellGroup], parentBounds: ObjectProperty[javafx.geometry.Bounds], numCols: IntegerProperty) {

    val grid = new GridPane {
      hgap = 10
      vgap = 10
    }

    numCols.onChange {
      (_, _, _) => update()
    }

    // regen to take into account current disabled/enabled value
    private def genNodes: Seq[VBox] =
      for {
        group <- cells
        if group.enabled.getOrElse(new BooleanProperty {
          value = true
        }).value
        cell <- group.cells
      } yield new VBox {
        children = Seq(
          new Label(cell.label),
          new Text {
            text <== cell.value
            style = "-fx-font-size: 200%"
          }
        )
        padding = Insets(20)
      }


    cells.map(_.enabled).distinct.foreach(_.foreach(_.onChange {
      (_,_, _) => update()
    }))

    update()

    def addThem(row: Int, toAdd: Seq[VBox], remaining: Seq[VBox]): Unit = {
      if (toAdd.isEmpty) return
      grid.addRow(row, toAdd.map(_.delegate): _*)
      val (l, r) = remaining.splitAt(numCols.value)
      addThem(row + 1, l, r)
    }

    def update(): Unit = {
      grid.children = Seq()
      val nodes = genNodes
      val (l, r) = nodes.splitAt(numCols.value)
      addThem(0, l, r)
    }


  }

  implicit def dynamicTable2(a: DynamicTable): Node = a.grid

  val split: Parent = genSplit

  val theScene: Scene = new Scene {
    root = split
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "Formica Controller"
    width = 600
    height = 450
    scene = theScene
  }

  // kill the background thread
  stage.onCloseRequest = (_: WindowEvent) => {
    Platform.exit()
    antNode.stop()
    System.exit(0)
  }

  // this stuff is last to ensure all variables are initialised

  private val progressStr = "in progress"

  val turbo = new FecProfile {
    override def onCapabilitiesReceived(capabilities: Capabilities): Unit = {
      updateCapabilites(capabilities)
    }

    override def onConfigRecieved(config: Config): Unit = {
      updateConfig(config)
    }

    override def onEquipmentStateChange(oldState: Defines.EquipmentState, newState: Defines.EquipmentState): Unit = {
      equipmentState.value.value = newState.toString()
    }



    override def onCalibrationUpdate(progress: CalibrationProgress): Unit = {
      calibrationSpeedState.value.value = Try(progress.getSpeedState()).map(_.toString()).getOrElse("null")
      calibrationTargetSpin.value.value = Try(progress.getTargetSpinDownTime()).map(_.toString()).getOrElse("null")
      calibrationTargetSpeed.value.value = Try(progress.getTargetSpeed()).map(_.toString()).getOrElse("null")
      calibrationSpinDown.value.value = if (progress.isSpinDownPending()) progressStr else unknownStr
      calibrationZeroOffset.value.value = if (progress.isOffsetPending()) progressStr else unknownStr
      tempCondition.value.value = Try(progress.getTempState()).map(_.toString()).getOrElse("null")
      temperature.value.value = temperatureToString(progress.getTemp())
    }

    override def onCalibrationStatusReceieved(calibrationResponse: CalibrationResponse) = {
      calibrationSpinDown.value.value = Try(calibrationResponse.getSpinDownTime()).map(_.toString()).getOrElse("null")
      calibrationZeroOffset.value.value = Try(calibrationResponse.getZeroOffset()).map(_.toString()).getOrElse("null")
      temperature.value.value = temperatureToString(calibrationResponse.getTemp())
      calibrationSuccess.value.value = boolToStr(calibrationResponse.isSpinDownSuccess() || calibrationResponse.isZeroOffsetSuccess)
    }

    override def onConnect() = {
      connected.value = true
    }

    override def onDisconnect() = {
      connected.value = false
    }

    override def onStatusChange(oldValue: util.EnumSet[Defines.TrainerStatusFlag], newValue: util.EnumSet[Defines.TrainerStatusFlag]) = {
      trainerStatusProp.value = newValue
    }
  }

  private def temperatureToString(progress: java.math.BigDecimal) = {
    Try(progress)
      .map((x: java.math.BigDecimal) => "%2.2f".format(x.floatValue()))
      .getOrElse("null")
  }

  val turboThread = new Thread() {
    override def run(): Unit = {
      println("Node starting")
      antNode.start()
      antNode.reset()
      turbo.start(antNode)
    }
  }

  private val prioritisedBus = new FilteredBroadcastMessenger[TaggedTelemetryEvent]()

  private val priorities = Array[EventPrioritiser.PrioritisedEvent](
    new PrioritisedEventBuilder(classOf[SpeedUpdate])
      .setTagPriorities(classOf[GeneralData], classOf[TorqueData])
      .createPrioritisedEvent(),
    new PrioritisedEventBuilder(classOf[DistanceUpdate])
        .setTagPriorities(classOf[GeneralData], classOf[TorqueData])
        .createPrioritisedEvent(),
    new PrioritisedEventBuilder(classOf[CoastEvent])
      .setTagPriorities(classOf[TrainerData], classOf[TorqueData])
      .createInheritedPrioritisedEvent()
  )


  private val prioritiser = new BufferedEventPrioritiser(prioritisedBus, priorities)

  turbo.getDataHub.addListener(classOf[TaggedTelemetryEvent], prioritiser)

  prioritisedBus.addListener(classOf[AveragePowerUpdate], (v: AveragePowerUpdate) => {
    powerAvg.value = "%2.2f".format(v.getAveragePower.floatValue())
  })

  prioritisedBus.addListener(classOf[InstantPowerUpdate], (v: InstantPowerUpdate) => {
    power.value = "%2.2f".format(v.getPower.floatValue())
  })

  prioritisedBus.addListener(classOf[DistanceUpdate], (v: DistanceUpdate) => {
    // raw value is in m, convert to km
    distance.value = "%2.2f".format(v.getDistance.floatValue() / 1000)
  })

  prioritisedBus.addListener(classOf[HeartRateUpdate], (v: HeartRateUpdate) => {
    hr.value = "%d".format(v.getHeartRate)
  })

  prioritisedBus.addListener(classOf[SpeedUpdate], (v: SpeedUpdate) => {
    speed.value = "%2.2f".format(v.getSpeed.floatValue())
  })

  prioritisedBus.addListener(classOf[CadenceUpdate], (v: CadenceUpdate) => {
    cadence.value = "%d".format(v.getCadence)
  })

  //prioritisedBus.addListener(classOf[TaggedTelemetryEvent], (v: TaggedTelemetryEvent) => {
    //println(s"tag:${v.getTag}, event:$v")
  //})

  prioritisedBus.addListener(classOf[CoastDetectedEvent], (v: CoastDetectedEvent) => {
    coastingPair.value.value = boolToStr(true)
  })

  prioritisedBus.addListener(classOf[CoastEndEvent], (v: CoastEndEvent) => {
    coastingPair.value.value = boolToStr(false)
  })

  prioritisedBus.addListener(classOf[LapUpdate], (v:LapUpdate) => {
    laps.value = "%d".format(v.getLaps())
  })


  turboThread.start()
}
