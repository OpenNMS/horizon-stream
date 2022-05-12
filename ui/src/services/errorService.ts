import { ResponseError } from '@/types'

const isResponseError = (err: unknown): err is ResponseError =>
  (err as ResponseError).response?.data?.error_description !== undefined

const getMsgFromError = (err: unknown, fallback: string = 'Could not complete request.'): string => {
  if (isResponseError(err)) {
    return err.response.data.error_description
  }

  return fallback
}

export { getMsgFromError }
