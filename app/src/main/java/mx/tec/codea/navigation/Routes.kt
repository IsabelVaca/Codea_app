package mx.tec.codea.navigation

import kotlinx.serialization.Serializable

// a route is the "address" of a screen inside the app.
// we use @Serializable objects instead of plain strings, so the compiler
// can check our routes for us and we avoid typos like "my_dya".

// the first screen the user sees when the app opens: the routine of today.
@Serializable
data object MyDayRoute

// the list of children in the room of the teacher.
@Serializable
data object MyRoomRoute

// the conversations with the families.
@Serializable
data object ChatsRoute

// the teacher profile, the check-in and other options.
@Serializable
data object MenuRoute
