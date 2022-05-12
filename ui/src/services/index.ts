import { login, logout, getUserInfo, refreshToken } from './authService'
import { sendEvent } from './eventService'
import { getAlarms, deleteAlarmById } from './alarmService'

export default { login, logout, getUserInfo, refreshToken, getAlarms, deleteAlarmById, sendEvent }
