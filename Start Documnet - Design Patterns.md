## **Start Documnet - Design Patterns**

### **Assignment: Smart Home Automation System**

The developers are developing a **Smart Home Automation System** that manages smart devices (such as lights, thermostats, and security cameras). The system should be able to:

- Use the **Facade Pattern** to provide a simple interface for managing devices.
- Dynamically create different types of smart devices using a **Factory Method Pattern**.
- Notify users when a device's state changes using the **user.Observer Pattern**.

------

### **Requirements**

#### **1. Facade Pattern (SmartHomeControllerUI)**

- **SmartHomeController** class that acts as a **facade** for managing all smart devices.
- It should have methods like:
  - `addDevice(String type, String name)`: Adds a new smart device using the factory.
  - `removeDevice(String name)`: Removes a device from the system.
  - `changeDeviceState(String name, String newState)`: Updates a device's state.
  - `subscribeUser(String deviceName, user.User user)`: Adds a user as an observer.
- Internally, the controller should use a `List<SmartDevice>` to store devices.

#### **2.  Factory Method Pattern (Smart Device Creation)**

- Define an **abstract class** `SmartDevice` with attributes like `deviceName` and `status`.
- Implement concrete classes:
  - `SmartLight` (on/off state)
  - `SmartThermostat` (temperature settings)
  - `SmartCamera` (recording on/off)
- Create a factory class `SmartDeviceFactory` with a method `createDevice(String type, String name)` that returns a corresponding `SmartDevice`.

#### **3. user.Observer Pattern (Device State Notifications)**

- Create an `user.Observer` interface with `update(String message)`.
- Implement a `user.User` class that subscribes to smart devices and receives notifications.
- Each `SmartDevice` should maintain a list of observers (users) and notify them when their status changes.
- The `addObserver(user.User user)` and `removeObserver(user.User user)` methods should allow users to subscribe/unsubscribe.



##### **Architecture:**

The camera system follows a MVVM (Model-View-ViewModel) architecture

**Class Diagram:**

https://github.com/Polinalavender/Design-patterns-/blob/main/Class%20Diagram%20design%20Patterns.asta


**MOSCOW analysis:**

| MUST have                                                    | SHOULD have                              | COULD have                                          | WON'T have                             |
| ------------------------------------------------------------ | ---------------------------------------- | --------------------------------------------------- | -------------------------------------- |
| Add/remove a user as an observer                             | Clears all devices                       | Retrieve the history of state change                | Support for voice assistants           |
| Add/remove devices                                           | `SmartDevice` stores a list of observers | Ability to group devices (e.g., "Living Room" group | A mobile interface for remote control. |
| Change device status                                         | Video door bells                         | Users can filter notifications                      |                                        |
| Checks if the device is active or disconnected               |                                          |                                                     |                                        |
| `user.User` class implementing `user.Observer`, receiving notifications |                                          |                                                     |                                        |



**Information about developers:**

| Developer name         | Email                                                        |
| ---------------------- | ------------------------------------------------------------ |
| Polina Zueva           | [polly.zueva@student.nhlstenden.com](mailto:polly.zueva@student.nhlstenden.com) |
| Ameli Masewge Fernando | <ameli.masewge.fernando@student.nhlstenden.com>              |
