declare module 'mammoth' {
  interface MammothResult {
    value: string
    messages: Array<{ type: string; message: string }>
  }

  interface MammothOptions {
    arrayBuffer: ArrayBuffer
  }

  export function extractRawText(options: MammothOptions): Promise<MammothResult>
}
