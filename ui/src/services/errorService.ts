import { DefaultResponseError, CustomResponseError } from '@/types'

const isDefaultResponseError = (err: unknown): err is DefaultResponseError =>
  (err as DefaultResponseError).response?.data?.error_description !== undefined

const isCustomResponseError = (err: unknown): err is CustomResponseError =>
  typeof (err as CustomResponseError).response?.data === 'string'

const getMsgFromError = (err: unknown, fallback = 'Could not complete request.'): string => {
  if (isDefaultResponseError(err)) {
    return err.response.data.error_description
  }

  if (isCustomResponseError(err)) {
    return err.response.data
  }

  return fallback
}

export { getMsgFromError }
