/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package values

type TLSValues struct {
	Enabled bool         `yaml:"Enabled"`
	Issuer  IssuerValues `yaml:"Issuer"`
	Cert    CertValues   `yaml:"Cert"`
	Solver  string       `yaml:"Solver"`
}

type IssuerValues struct {
	IssuerServer    string `yaml:"IssuerServer"`
	IssuerUserEmail string `yaml:"IssuerUserEmail"`
}

type CertValues struct {
	Name     string `yaml:"Name"`
	Hostname string `yaml:"Hostname"`
}
