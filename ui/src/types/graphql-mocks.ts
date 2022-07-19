import { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
};

export type Device = {
  __typename?: 'Device';
  icmp_latency: Scalars['String'];
  id: Scalars['String'];
  name: Scalars['String'];
  snmp_uptime: Scalars['String'];
  status: Scalars['String'];
};

export type DeviceInput = {
  community_string?: InputMaybe<Scalars['String']>;
  icmp_latency?: InputMaybe<Scalars['String']>;
  management_ip?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  port?: InputMaybe<Scalars['Int']>;
  snmp_uptime?: InputMaybe<Scalars['String']>;
};

export type ListDevices = {
  __typename?: 'ListDevices';
  count: Scalars['String'];
  items: Array<Device>;
  offset: Scalars['String'];
  totalCount: Scalars['String'];
};

export type ListMinions = {
  __typename?: 'ListMinions';
  minions: Array<Minion>;
};

export type Minion = {
  __typename?: 'Minion';
  icmp_latency: Scalars['String'];
  id: Scalars['String'];
  label: Scalars['String'];
  lastUpdated: Scalars['String'];
  location: Scalars['String'];
  snmp_uptime: Scalars['String'];
  status: Scalars['String'];
};

export type Mutation = {
  __typename?: 'Mutation';
  saveDevice?: Maybe<Scalars['String']>;
  saveRoutingKey?: Maybe<Scalars['String']>;
};


export type MutationSaveDeviceArgs = {
  device: DeviceInput;
};


export type MutationSaveRoutingKeyArgs = {
  key: Scalars['String'];
};

export type Query = {
  __typename?: 'Query';
  device: Device;
  listDevices: ListDevices;
  listMinions: ListMinions;
  minion: Minion;
};

export type SaveDeviceMutationVariables = Exact<{
  device: DeviceInput;
}>;


export type SaveDeviceMutation = { __typename?: 'Mutation', saveDevice?: string | null };

export type SaveRoutingKeyMutationVariables = Exact<{
  key: Scalars['String'];
}>;


export type SaveRoutingKeyMutation = { __typename?: 'Mutation', saveRoutingKey?: string | null };

export type ListDevicesQueryVariables = Exact<{ [key: string]: never; }>;


export type ListDevicesQuery = { __typename?: 'Query', listDevices: { __typename?: 'ListDevices', count: string, totalCount: string, offset: string, items: Array<{ __typename?: 'Device', id: string, name: string, status: string, icmp_latency: string, snmp_uptime: string }> } };

export type ListMinionsQueryVariables = Exact<{ [key: string]: never; }>;


export type ListMinionsQuery = { __typename?: 'Query', listMinions: { __typename?: 'ListMinions', minions: Array<{ __typename?: 'Minion', id: string, label: string, status: string, location: string, lastUpdated: string, icmp_latency: string, snmp_uptime: string }> } };


export const SaveDeviceDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"SaveDevice"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"device"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"DeviceInput"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"saveDevice"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"device"},"value":{"kind":"Variable","name":{"kind":"Name","value":"device"}}}]}]}}]} as unknown as DocumentNode<SaveDeviceMutation, SaveDeviceMutationVariables>;
export const SaveRoutingKeyDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"SaveRoutingKey"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"key"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"saveRoutingKey"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"key"},"value":{"kind":"Variable","name":{"kind":"Name","value":"key"}}}]}]}}]} as unknown as DocumentNode<SaveRoutingKeyMutation, SaveRoutingKeyMutationVariables>;
export const ListDevicesDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"ListDevices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"listDevices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"items"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"name"}},{"kind":"Field","name":{"kind":"Name","value":"status"}},{"kind":"Field","name":{"kind":"Name","value":"icmp_latency"}},{"kind":"Field","name":{"kind":"Name","value":"snmp_uptime"}}]}},{"kind":"Field","name":{"kind":"Name","value":"count"}},{"kind":"Field","name":{"kind":"Name","value":"totalCount"}},{"kind":"Field","name":{"kind":"Name","value":"offset"}}]}}]}}]} as unknown as DocumentNode<ListDevicesQuery, ListDevicesQueryVariables>;
export const ListMinionsDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"ListMinions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"listMinions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"minions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"label"}},{"kind":"Field","name":{"kind":"Name","value":"status"}},{"kind":"Field","name":{"kind":"Name","value":"location"}},{"kind":"Field","name":{"kind":"Name","value":"lastUpdated"}},{"kind":"Field","name":{"kind":"Name","value":"icmp_latency"}},{"kind":"Field","name":{"kind":"Name","value":"snmp_uptime"}}]}}]}}]}}]} as unknown as DocumentNode<ListMinionsQuery, ListMinionsQueryVariables>;