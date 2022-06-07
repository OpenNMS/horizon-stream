import { logout } from './authService'
import { sendEvent } from './eventService'
import { getAlarms, deleteAlarmById } from './alarmService'

export default { logout, getAlarms, deleteAlarmById, sendEvent }
